package nav.portal.jobs.recordAggregation;

import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.RecordEntity;

import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.RecordRepository;

import nav.portal.core.repositories.ServiceRepository;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.fluentjdbc.DbTransaction;

import javax.sql.DataSource;
import java.time.Instant;
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
                getOldRecords_AggregateAndSave();
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


    private void getOldRecords_AggregateAndSave(){
        List<RecordEntity> outdatedRecords = recordRepository.getRecordsOlderThan(AGGREGATE_RECORDS_OLDER_THAN_NUMBER_OF_DAYS);


        Map<Integer,List<RecordEntity>> dayOfYearWithUUIDandRecords = new HashMap<>();

        while (outdatedRecords.size() > 0){
            Integer dayOfYear = outdatedRecords.get(0).getCreated_at().getDayOfYear();
            dayOfYearWithUUIDandRecords.put(dayOfYear
                    ,filterByDayOfyear(outdatedRecords,dayOfYear));
            outdatedRecords = filterOutProssesedReckords(outdatedRecords, dayOfYear);
        }

        dayOfYearWithUUIDandRecords.values().forEach(this::compressOneDay);

    }

    private List<RecordEntity> filterOutProssesedReckords(List<RecordEntity> outdatedRecords, Integer dayOfYear) {
        return outdatedRecords.stream().filter(r -> !dayOfYear.equals(r.getCreated_at().getDayOfYear())).collect(Collectors.toList());
    }

    private void compressOneDay(List<RecordEntity> recordsForOneDay){
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

        serviceUUIDRecordsMap.entrySet().forEach(this::compressAndSave);
    }


    private void compressAndSave(Map.Entry<UUID, List<RecordEntity>> serviceEntry) {
        DailyStatusAggregationForServiceEntity aggregatedRecords = new DailyStatusAggregationForServiceEntity();
        aggregatedRecords.setService_id(serviceEntry.getKey());
        aggregatedRecords.setAggregation_date(serviceEntry.getValue().get(0).getCreated_at().toLocalDate());
        aggregatedRecords.setNumber_of_status_down((int) getCount(serviceEntry.getValue(), ServiceStatus.DOWN));
        aggregatedRecords.setNumber_of_status_issue((int) getCount(serviceEntry.getValue(), ServiceStatus.ISSUE));
        aggregatedRecords.setNumber_of_status_ok((int) getCount(serviceEntry.getValue(), ServiceStatus.OK));
        recordRepository.saveAggregatedRecords(aggregatedRecords);
    }

    private long getCount(List<RecordEntity> recordEntities, ServiceStatus status){
        return recordEntities.stream().filter(r -> r.getStatus().equals(status)).count();
    }


}
