package nav.portal.jobs.recordAggregation;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceStatus;
import javax.sql.DataSource;

import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import org.fluentjdbc.*;


import java.time.*;
import java.util.*;


public class MockDataGenerator {
    private final static Integer INTERVALL_BETWEEN_STATUS_UPDATES_MINUTES = 60;
    private final static int NUMBER_OF_DAYS = 100;



    private static UUID save(RecordEntity entity, DbContextTable recordTable) {

        DatabaseSaveResult<UUID> result = recordTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getServiceId())
                .setField("created_at", entity.getCreated_at())
                .setField("status", entity.getStatus())
                .setField("description", "GENERATED MOCK")
                .setField("logglink", entity.getLogglink())
                .setField("response_time", entity.getResponsetime())
                .execute();

        return result.getId();
    }


    public static void generateRandomStatusesForAllServices(DbContext dbContext, DataSource dataSource){
        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {

            try (DbTransaction transaction = dbContext.ensureTransaction()) {
                generateRandomStatusesForAllServicesInternal(dbContext);
                transaction.setComplete();
            }
        }

    }

    private static void generateRandomStatusesForAllServicesInternal(DbContext dbContext){
        ServiceRepository serviceRepository = new ServiceRepository(dbContext);
        RecordRepository recordRepository = new RecordRepository(dbContext);

        List<ServiceEntity> allServices = new ArrayList<>();
        allServices.addAll(new ArrayList<>(serviceRepository.retrieveAllServices().keySet()));
        Map<UUID,Map<Integer,List<RecordEntity>>> recordsToInsert = new HashMap<>();

        allServices.forEach(service -> recordsToInsert.put(service.getId()
                ,generateRandomStatusesForOneServiceXNumberOfDaysBackInTime(NUMBER_OF_DAYS,service)));


        DbContextTable recordTable = dbContext.table(new DatabaseTableImpl("service_status"));


        recordsToInsert.values().forEach(
                allRecordsForOneService -> allRecordsForOneService.values().forEach(
                        allRecordsForOneServiceOneDay ->
                                allRecordsForOneServiceOneDay.forEach(
                                        recordEntity -> MockDataGenerator.save(recordEntity,recordTable)
                                )
                )
        );

    }


    private static  Map<Integer,List<RecordEntity>> generateRandomStatusesForOneServiceXNumberOfDaysBackInTime(int numberOfDays, ServiceEntity serviceEntity) {
        Map<Integer,List<RecordEntity>> result = new HashMap<>();
        while(numberOfDays>0){
            result.put(numberOfDays,generateRandomStatusesForServiceForOneDayXNumberOfDaysBackInTime(serviceEntity, numberOfDays));
            numberOfDays--;
        }
        return result;
    }
    private static List<RecordEntity> generateRandomStatusesForServiceForOneDayXNumberOfDaysBackInTime(ServiceEntity serviceEntity,int numberOfDays){

        int NUMBER_OF_STATUS = 24*60/INTERVALL_BETWEEN_STATUS_UPDATES_MINUTES;
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(numberOfDays),  LocalTime.of(0,0,0,0));
        ZonedDateTime creation_time = ZonedDateTime.of(startTime, ZoneId.of("Europe/Paris"));
        int numberOfStatusesGanerated = 0;
        List<RecordEntity> result = new ArrayList<>();
        while (NUMBER_OF_STATUS > numberOfStatusesGanerated){
            result.add(generateRandomStatus(serviceEntity, creation_time));
            creation_time = creation_time.plusMinutes(INTERVALL_BETWEEN_STATUS_UPDATES_MINUTES);
            numberOfStatusesGanerated++;

        }
        return result;

    }

    private static  RecordEntity generateRandomStatus(ServiceEntity serviceEntity, ZonedDateTime creation_time) {
        RecordEntity recordEntity = new RecordEntity();
        recordEntity.setServiceId(serviceEntity.getId());
        recordEntity.setStatus(getRandomServiceStatus());
        recordEntity.setCreated_at(creation_time);
        return recordEntity;
    }

    private static ServiceStatus getRandomServiceStatus() {
        int random = (new Random()).nextInt(100);
        if(random> 98){
            return ServiceStatus.DOWN;
        }
        if(random> 96){
            return ServiceStatus.ISSUE;
        }
        return ServiceStatus.OK;
    }



}
