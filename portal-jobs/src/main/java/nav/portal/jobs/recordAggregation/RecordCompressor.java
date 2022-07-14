package nav.portal.jobs.recordAggregation;

import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.RecordEntity;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.RecordRepository;

import nav.portal.core.repositories.ServiceRepository;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.fluentjdbc.DbTransaction;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RecordCompressor extends Thread{

    private final RecordRepository recordRepository;
    private final ServiceRepository serviceRepository;
    private final DbContext dbContext;
    private DataSource dataSource;
    private final int KEEP_RECORDS_FOR_THIS_NUMBER_OF_DAYS = 7;
    private final int AGGREGATE_RECORDS_OLDER_THAN_NUMBER_OF_DAYS = 1; //This does NOT delete records older than



    public RecordCompressor(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.dbContext = dbContext;
    }
    public void run(){
        startCompression();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void startCompression() {

        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {
            try (DbTransaction transaction = dbContext.ensureTransaction()) {
                getOldRecordsAggregateAndSave();
                deleteOldRecords();
                transaction.setComplete();
            }
        }
    }

    private void deleteOldRecords() {
        recordRepository.deleteRecordsOlderThen(KEEP_RECORDS_FOR_THIS_NUMBER_OF_DAYS);
    }


    private List<RecordEntity> filterByDayOfyear(List<RecordEntity> records, Integer dayOfYear){
        return records.stream().filter(r2 -> dayOfYear.equals(r2.getCreated_at().getDayOfYear()))
                .collect(Collectors.toList());
    }

    private void getOldRecordsAggregateAndSave(){
        List<UUID> serviceUUIDS = serviceRepository.retrieveAllShallow().stream().map(ServiceEntity::getId).collect(Collectors.toList());
        List<RecordEntity> outdatedRecords = recordRepository.getRecordsOlderThan(AGGREGATE_RECORDS_OLDER_THAN_NUMBER_OF_DAYS);

        Map<UUID,List<RecordEntity>> serviceID_recordsMap = new HashMap<>();
        //TODO dette mappet kan lages direkte fra en spørring i repo
        for(UUID uuid: serviceUUIDS){
            serviceID_recordsMap.put(uuid,
                    outdatedRecords.stream()
                            .filter(recordEntity ->
                                    recordEntity.getServiceId().equals(uuid))
                            .collect(Collectors.toList()));
        }

        serviceID_recordsMap.forEach(this::compressAndSave);
    }

    private void compressAndSave(UUID serviceID, List<RecordEntity> outdatedRecords) {
        if(outdatedRecords.isEmpty()){
            //Dersom en tjeneste ikke har fått inn noen statuser lages det et tomt historyobjet for en  dag
            createEmptyHistoryForServiceForOneDay(serviceID);
        }
        Map<Integer,List<RecordEntity>> dayOfYearWithUUIDandRecords = new HashMap<>();

        while (outdatedRecords.size() > 0){
            Integer dayOfYear = outdatedRecords.get(0).getCreated_at().getDayOfYear();
            dayOfYearWithUUIDandRecords.put(dayOfYear
                    ,filterByDayOfyear(outdatedRecords,dayOfYear));
            outdatedRecords = filterOutProssesedReckords(outdatedRecords, dayOfYear);
        }

        dayOfYearWithUUIDandRecords.values().forEach(this::compressOneDayAndSaveResult);
    }




    private List<RecordEntity> filterOutProssesedReckords(List<RecordEntity> outdatedRecords, Integer dayOfYear) {
        return outdatedRecords.stream().filter(r -> !dayOfYear.equals(r.getCreated_at().getDayOfYear())).collect(Collectors.toList());
    }

    private void compressOneDayAndSaveResult(List<RecordEntity> recordsForOneDay){
        //Her sjekker vi først om det finnes en aggregert status for dagen:

        if (isAlreadyHandled(recordsForOneDay)) return;

        Map<UUID,List<RecordEntity>> serviceUUIDRecordsMap = new HashMap<>();
        while (recordsForOneDay.size() > 0 ){
            RecordEntity record = recordsForOneDay.get(0);

            serviceUUIDRecordsMap.put(record.getServiceId(),
                    recordsForOneDay.stream()
                            .filter(r -> r.getServiceId().equals(record.getServiceId()))
                            .collect(Collectors.toList()));
            recordsForOneDay = recordsForOneDay
                    .stream().filter(r -> !r.getServiceId().equals(record.getServiceId())).collect(Collectors.toList());
        }

        serviceUUIDRecordsMap.forEach(this::createAndSaveAggregation);
    }

    private boolean isAlreadyHandled(List<RecordEntity> recordsForOneDay) {
        Optional<RecordEntity> recordSample = recordsForOneDay.stream().findFirst();
        if(recordSample.isPresent()) {
            Optional<DailyStatusAggregationForServiceEntity> aggregation = recordRepository.
                    getServiceHistoryForServiceByDate(recordSample.get().getServiceId(), recordSample.get().getCreated_at().toLocalDate());
            if(aggregation.isPresent()){
                return true;
            }
        }
        return false;
    }


    private void createAndSaveAggregation(UUID serviceID, List<RecordEntity> records) {
        DailyStatusAggregationForServiceEntity aggregatedRecords = new DailyStatusAggregationForServiceEntity();
        aggregatedRecords.setService_id(serviceID);
        aggregatedRecords.setAggregation_date(records.get(0).getCreated_at().toLocalDate());
        aggregatedRecords.setNumber_of_status_down((int) getCount(records, ServiceStatus.DOWN));
        aggregatedRecords.setNumber_of_status_issue((int) getCount(records, ServiceStatus.ISSUE));
        aggregatedRecords.setNumber_of_status_ok((int) getCount(records, ServiceStatus.OK));
        recordRepository.saveAggregatedRecords(aggregatedRecords);
    }

    private void createEmptyHistoryForServiceForOneDay(UUID serviceID) {
        DailyStatusAggregationForServiceEntity aggregatedRecords = new DailyStatusAggregationForServiceEntity();
        aggregatedRecords.setService_id(serviceID);
        aggregatedRecords.setAggregation_date(LocalDate.now().minusDays(AGGREGATE_RECORDS_OLDER_THAN_NUMBER_OF_DAYS));
        recordRepository.saveAggregatedRecords(aggregatedRecords);
    }

    private long getCount(List<RecordEntity> recordEntities, ServiceStatus status){
        return recordEntities.stream().filter(r -> r.getStatus().equals(status)).count();
    }


}
