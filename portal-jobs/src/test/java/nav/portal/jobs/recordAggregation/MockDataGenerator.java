package nav.portal.jobs.recordAggregation;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceStatus;

import org.fluentjdbc.*;


import java.time.*;
import java.util.*;


public class MockDataGenerator {

    static UUID save(RecordEntity entity, DbContextTable recordTable) {

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

/**
    public static void generateRandomStatusesForAllServices(DbContext dbContext, DataSource dataSource){
        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {

            try (DbTransaction transaction = dbContext.ensureTransaction()) {
                generateRandomStatusesForAllServicesInternal(dbContext);
                transaction.setComplete();
            }
        }

    }**/

    public static Map<UUID,Map<Integer,List<RecordEntity>>>  generateRandomStatusesForAllServices(List<ServiceEntity> allServices, int number_of_days, int interval_between_status_update_minutes){

        Map<UUID,Map<Integer,List<RecordEntity>>> recordsToInsert = new HashMap<>();

        allServices.forEach(service -> recordsToInsert.put(service.getId()
                ,generateRandomStatusesForOneServiceXNumberOfDaysBackInTime(service, number_of_days, interval_between_status_update_minutes)));

         return recordsToInsert;

     }

    static void saveRecordsToTable(Map<UUID, Map<Integer, List<RecordEntity>>> recordsToInsert, DbContext dbContext) {
        DbContextTable recordTable = dbContext.table(new DatabaseTableImpl("service_status"));
        recordsToInsert.values().forEach(
                allRecordsForOneService -> allRecordsForOneService.values().forEach(
                        allRecordsForOneServiceOneDay ->
                                allRecordsForOneServiceOneDay.forEach(
                                        recordEntity -> MockDataGenerator.save(recordEntity, recordTable)
                                )
                )
        );
    }


    public static  Map<Integer,List<RecordEntity>> generateRandomStatusesForOneServiceXNumberOfDaysBackInTime(ServiceEntity serviceEntity, int numberOfDays, int interval_between_status_update_minutes) {
        Map<Integer,List<RecordEntity>> result = new HashMap<>();
        while(numberOfDays>0){
            result.put(numberOfDays,generateRandomStatusesForServiceForOneDayXNumberOfDaysBackInTime(serviceEntity, numberOfDays, interval_between_status_update_minutes));
            numberOfDays--;
        }
        return result;
    }
    private static List<RecordEntity> generateRandomStatusesForServiceForOneDayXNumberOfDaysBackInTime(ServiceEntity serviceEntity,int numberOfDays, int interval_between_status_update_minutes){

        int NUMBER_OF_STATUS = 24*60/interval_between_status_update_minutes;
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(numberOfDays),  LocalTime.of(0,0,0,0));
        ZonedDateTime creation_time = ZonedDateTime.of(startTime, ZoneId.of("Europe/Paris"));
        int numberOfStatusesGanerated = 0;
        List<RecordEntity> result = new ArrayList<>();
        while (NUMBER_OF_STATUS > numberOfStatusesGanerated){
            result.add(generateRandomStatus(serviceEntity, creation_time));
            creation_time = creation_time.plusMinutes(interval_between_status_update_minutes);
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
