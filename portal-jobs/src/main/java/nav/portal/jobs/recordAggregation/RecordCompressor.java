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




    public RecordCompressor(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.dbContext = dbContext;
    }
    public void run(){
        Thread.currentThread().setUncaughtExceptionHandler(new CompressionThreadExceptionHandler(dbContext,dataSource));
        try{
            startCompression();
        }
        catch (Exception e){
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),e);
        }

    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void startCompression() {

        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {
            try (DbTransaction transaction = dbContext.ensureTransaction()) {
                getRecordsAggregateAndSave();
                transaction.setComplete();
            }
        }
    }

    private void getRecordsAggregateAndSave(){
        Map<UUID, Map<LocalDate,List<RecordEntity>>> allRecords = recordRepository.getAllRecordsOrderedByServiceIdAndDate();
        allRecords.forEach(this::compressAndSaveForOneService);


    }

    private void compressAndSaveForOneService(UUID serviceID, Map<LocalDate,List<RecordEntity>> recordsByDate) {
        recordsByDate.keySet().forEach(date -> {
                    if(date.equals(LocalDate.now())){
                        return;
                    }
                    compressAndSaveForOneServiceOneDay(serviceID,recordsByDate.get(date),date);
                });

    }

    private void compressAndSaveForOneServiceOneDay(UUID serviceID, List<RecordEntity> records, LocalDate aggregationDate) {
        //Lagrer til diff table:
        saveStatusChangesForDate(serviceID,records, aggregationDate);

        //Dersom en tjeneste ikke har fått inn noen statuser lages det et tomt historyobjekt for en  dag
        if(records.isEmpty()){
            createEmptyHistoryForServiceForOneDay(serviceID, aggregationDate);
        }
        else {
            compressOneDayAndSaveResult(records);
            recordRepository.deleteRecords(records);
        }
    }


    private void saveStatusChangesForDate(UUID serviceID, List<RecordEntity> records, LocalDate date) {
        ServiceStatus latestStatus = recordRepository.getLatestRecordDiffBeforeDate(serviceID, date).map(RecordEntity::getStatus).orElse(null);

        for(RecordEntity record : records){
            if(!record.getStatus().equals(latestStatus)){
                recordRepository.saveDiff(record);
                latestStatus = record.getStatus();
            }

        }
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

    private void createEmptyHistoryForServiceForOneDay(UUID serviceID, LocalDate aggragatonDate) {
        DailyStatusAggregationForServiceEntity aggregatedRecords = new DailyStatusAggregationForServiceEntity();
        aggregatedRecords.setService_id(serviceID);
        aggregatedRecords.setAggregation_date(aggragatonDate);
        recordRepository.saveAggregatedRecords(aggregatedRecords);
    }

    private long getCount(List<RecordEntity> recordEntities, ServiceStatus status){
        return recordEntities.stream().filter(r -> r.getStatus().equals(status)).count();
    }


}
