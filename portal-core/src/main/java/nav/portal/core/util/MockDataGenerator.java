package nav.portal.core.util;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceStatus;

import nav.portal.core.repositories.RecordRepository;
import org.fluentjdbc.*;


import java.time.*;
import java.util.*;


public class MockDataGenerator {


    public static Map<UUID,Map<Integer,List<RecordEntity>>>  generateRandomStatusesForAllServices(List<ServiceEntity> allServices, int number_of_days, int interval_between_status_update_minutes){

        Map<UUID,Map<Integer,List<RecordEntity>>> recordsToInsert = new HashMap<>();

        allServices.forEach(service -> recordsToInsert.put(service.getId()
                ,generateRandomStatusesForOneServiceXNumberOfDaysBackInTime(service, number_of_days, interval_between_status_update_minutes)));

         return recordsToInsert;

     }

    public static void saveRecordsToTableForAllServices(Map<UUID, Map<Integer, List<RecordEntity>>> recordsToInsert, DbContext dbContext) {
        recordsToInsert.values().forEach(
                allRecordsForOneService -> allRecordsForOneService.values().forEach(
                        allRecordsForOneServiceOneDay ->
                                allRecordsForOneServiceOneDay.forEach(
                                        recordEntity -> RecordRepository.saveRecordBackInTime(recordEntity, dbContext)
                                )
                )
        );
    }

    public static void saveRecordsToTableForOneService(Map<Integer, List<RecordEntity>> recordsToInsert, DbContext dbContext) {
        recordsToInsert.values().forEach(
                        allRecordsForOneServiceOneDay ->
                                allRecordsForOneServiceOneDay.forEach(
                                        recordEntity -> RecordRepository.saveRecordBackInTime(recordEntity, dbContext)
                                )

        );
    }
    public static void saveRecordsToTableForOneServiceOneDay(List<RecordEntity> recordsToInsert, DbContext dbContext) {
        recordsToInsert.forEach(
                recordEntity -> RecordRepository.saveRecordBackInTime(recordEntity, dbContext)
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
    public static List<RecordEntity> generateRandomStatusesForServiceForOneDayXNumberOfDaysBackInTime(ServiceEntity serviceEntity,int numberOfDaysBack, int interval_between_status_update_minutes){
        int MINUTES_IN_A_DAY = 24*60;
        int NUMBER_OF_STATUS = MINUTES_IN_A_DAY/interval_between_status_update_minutes;
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(numberOfDaysBack),  LocalTime.of(0,0,0,0));
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

    public static  RecordEntity generateRandomStatus(ServiceEntity serviceEntity, ZonedDateTime creation_time) {
        RecordEntity recordEntity = new RecordEntity();
        recordEntity.setServiceId(serviceEntity.getId());
        recordEntity.setStatus(getRandomServiceStatus());
        recordEntity.setCreated_at(creation_time);
        return recordEntity;
    }

    private static ServiceStatus getRandomServiceStatus() {
        int random1 = (new Random()).nextInt(100);
        int random2 = (new Random()).nextInt(100);
        if(random1> 96){
            if(random2>80){
                return ServiceStatus.DOWN;
            }
            else return ServiceStatus.ISSUE;
        }
        return ServiceStatus.OK;
    }






}
