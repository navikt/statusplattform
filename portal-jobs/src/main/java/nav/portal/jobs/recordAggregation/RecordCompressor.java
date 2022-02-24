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
import java.util.*;
import java.util.stream.Collectors;

public class RecordCompressor extends Thread{

    private final RecordRepository recordRepository;
    private final ServiceRepository serviceRepository;
    private final DbContext dbContext;
    private DataSource dataSource;
    private final int KEEP_RECORDS_FOR_THIS_NUMBER_OF_DAYS = 7;


    public RecordCompressor(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.dbContext = dbContext;
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

    private void getOldRecords_AggregateAndSave(){

        List<RecordEntity> outdatedRecords = recordRepository.getRecordsOlderThen(KEEP_RECORDS_FOR_THIS_NUMBER_OF_DAYS);
        Map<UUID,List<RecordEntity>> serviceUUIDRecordsMap =new HashMap<>();
        while (outdatedRecords.size()>0){
            RecordEntity record = outdatedRecords.get(0);

            serviceUUIDRecordsMap.put(record.getServiceId(),
                    outdatedRecords.stream()
                            .filter(r -> r.getServiceId().equals(record.getServiceId()))
                            .collect(Collectors.toList()));
            outdatedRecords = outdatedRecords
                    .stream().filter(r -> !r.getServiceId().equals(record.getServiceId())).collect(Collectors.toList());
        }
        
        serviceUUIDRecordsMap.entrySet().forEach(this::handleCompressAndDeleteOfOldData);

    }

    private void handleCompressAndDeleteOfOldData(Map.Entry<UUID, List<RecordEntity>> serviceEntry){
        compressAndSave(serviceEntry);

    }

    private void compressAndSave(Map.Entry<UUID, List<RecordEntity>> serviceEntry) {
        DailyStatusAggregationForServiceEntity aggregatedRecords = new DailyStatusAggregationForServiceEntity();
        aggregatedRecords.setId(serviceEntry.getKey());
        aggregatedRecords.setAggregation_date(Date.from(Instant.now()));
        aggregatedRecords.setNumber_of_status_down((int) getCount(serviceEntry.getValue(), ServiceStatus.DOWN));
        aggregatedRecords.setNumber_of_status_issue((int) getCount(serviceEntry.getValue(), ServiceStatus.ISSUE));
        aggregatedRecords.setNumber_of_status_ok((int) getCount(serviceEntry.getValue(), ServiceStatus.OK));
        recordRepository.saveAggregatedRecords(aggregatedRecords);
    }

    private long getCount(List<RecordEntity> recordEntities, ServiceStatus status){
        return recordEntities.stream().filter(r -> r.getStatus().equals(status)).count();
    }

    public void run(){
        startCompression();

    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
