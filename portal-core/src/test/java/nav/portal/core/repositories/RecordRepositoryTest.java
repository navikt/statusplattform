package nav.portal.core.repositories;


import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.entities.RecordEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

class RecordRepositoryTest {

    private final DataSource dataSource = TestDataSource.create();

    private final DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    private final RecordRepository recordRepository = new RecordRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

   @Test
    void save() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = SampleData.getRandomizedRecordEntity();
        record.setServiceId(service.getId());
        //Act
        record.setId(recordRepository.save(record));
        Optional<RecordEntity>retrievedEntity = recordRepository.getLatestRecord(serviceId);
        record.setCreated_at(retrievedEntity.orElseThrow().getCreated_at());
        //Assert
        Assertions.assertThat(retrievedEntity.get()).isEqualTo(record);
     }

    @Test
    void saveNewStatusDiff() {
         //Arrange
         ServiceEntity service = SampleData.getRandomizedServiceEntity();
         UUID serviceId = serviceRepository.save(service);
         service.setId(serviceId);
         RecordEntity record = SampleData.getRandomizedRecordEntityForService(service);
         record.setServiceId(service.getId());
         record.setDescription("newly generated");
         record.setActive(true);
         record.setId(recordRepository.save(record));
         UUID recordId = record.getId();
         Optional<RecordEntity>retrievedRecordBefore = recordRepository.getLatestRecord(serviceId);
         //Act
         UUID newStatusDiffId = recordRepository.saveNewStatusDiff(record);
         Optional<RecordEntity> retrievedRecordDiff = recordRepository.getLatestRecordDiff(serviceId);
         //Arrange
         Assertions.assertThat(retrievedRecordDiff.isPresent()).isTrue();
         Assertions.assertThat(retrievedRecordBefore.isPresent()).isTrue();
         Assertions.assertThat(retrievedRecordDiff.get().getId()).isEqualTo(retrievedRecordBefore.get().getId());
         Assertions.assertThat(retrievedRecordDiff.get().getCounter()).isEqualTo(1);
    }

    @Test
    void saveOldStatusDiff() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = SampleData.getRandomizedRecordEntityForService(service);
        record.setServiceId(service.getId());
        record.setDescription("newly generated");
        record.setActive(true);
        record.setId(recordRepository.save(record));
        UUID recordId = record.getId();
        UUID newStatusDiffId = recordRepository.saveNewStatusDiff(record);
        Optional<RecordEntity>retrievedRecordDiffBefore = recordRepository.getLatestRecordDiff(serviceId);
        //Act
        UUID oldStatusDiffId = null;
        if(retrievedRecordDiffBefore.isPresent()){
            oldStatusDiffId = recordRepository.saveOldStatusDiff(retrievedRecordDiffBefore.get());
        }
        Optional<RecordEntity> retrievedRecordDiffAfter = recordRepository.getLatestRecordDiff(serviceId);
        //Assert
        Assertions.assertThat(retrievedRecordDiffBefore.isPresent()).isTrue();
        Assertions.assertThat(retrievedRecordDiffAfter.isPresent()).isTrue();
        Assertions.assertThat(retrievedRecordDiffAfter.get().getId()).isEqualTo(retrievedRecordDiffBefore.get().getId());
        Assertions.assertThat(retrievedRecordDiffBefore.get().getActive()).isTrue();
        Assertions.assertThat(retrievedRecordDiffAfter.get().getActive()).isFalse();
     }

    @Test
    void increaseCountOnStatusDiff() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = SampleData.getRandomizedRecordEntityForService(service);
        record.setServiceId(service.getId());
        record.setDescription("newly generated");
        record.setActive(true);
        record.setId(recordRepository.save(record));
        UUID recordId = record.getId();
        UUID newStatusDiffId = recordRepository.saveNewStatusDiff(record);
        Optional<RecordEntity>retrievedRecordDiffBefore = recordRepository.getLatestRecordDiff(serviceId);
        //Act
        retrievedRecordDiffBefore.ifPresent(recordRepository::increaseCountOnStatusDiff);
        Optional<RecordEntity> retrievedRecordDiffAfter = recordRepository.getLatestRecordDiff(serviceId);
        //Arrange
        Assertions.assertThat(retrievedRecordDiffBefore.isPresent()).isTrue();
        Assertions.assertThat(retrievedRecordDiffAfter.isPresent()).isTrue();
        Assertions.assertThat(retrievedRecordDiffAfter.get().getId()).isEqualTo(retrievedRecordDiffBefore.get().getId());
        Assertions.assertThat(retrievedRecordDiffBefore.get().getCounter()).isEqualTo(1);
        Assertions.assertThat(retrievedRecordDiffAfter.get().getCounter()).isEqualTo(2);
    }

    @Test
    void getLatestRecord() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = SampleData.getRandomizedRecordEntity();
        record.setServiceId(service.getId());
        record.setId(recordRepository.save(record));
        //Act
        Optional<RecordEntity> retrievedRecord = recordRepository.getLatestRecord(service.getId());
        record.setCreated_at(retrievedRecord.orElseThrow().getCreated_at());
        //Assert
        Assertions.assertThat(retrievedRecord.get()).isEqualTo(record);
    }

    @Test
    void getServiceHistoryForNumberOfDays() {
        //Arrange
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        serviceEntity.setId(serviceRepository.save(serviceEntity));
        DailyStatusAggregationForServiceEntity aggregation = SampleData.getRandomizedDailyStatusAggregationForService(serviceEntity);
        aggregation.setAggregation_date(LocalDate.now().minusDays(5));

        recordRepository.saveAggregatedRecords(aggregation);

        UUID serviceID = serviceEntity.getId();
        //Act
        List<DailyStatusAggregationForServiceEntity> shouldBeEmpty = recordRepository.getServiceHistoryForNumberOfDays(4,serviceID);
        List<DailyStatusAggregationForServiceEntity> shouldContainOne = recordRepository.getServiceHistoryForNumberOfDays(10,serviceID);

        //Assert
        Assertions.assertThat(shouldBeEmpty).isEmpty();
        Assertions.assertThat(shouldContainOne.size()).isEqualTo(1);

    }

    @Test
    void getServiceHistoryForServiceByDate() {
        //Arrange
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        serviceEntity.setId(serviceRepository.save(serviceEntity));
        DailyStatusAggregationForServiceEntity aggregation = SampleData.getRandomizedDailyStatusAggregationForService(serviceEntity);
        aggregation.setAggregation_date(LocalDate.now().minusDays(50));

        recordRepository.saveAggregatedRecords(aggregation);
        UUID serviceId = serviceEntity.getId();
        //Act
        Optional<DailyStatusAggregationForServiceEntity> shouldBeEmpty =
                recordRepository.getServiceHistoryForServiceByDate(serviceId, LocalDate.now().minusDays(40));
        Optional<DailyStatusAggregationForServiceEntity> shouldContainOne =
                recordRepository.getServiceHistoryForServiceByDate(serviceId, LocalDate.now().minusDays(50));
        //Assert
        Assertions.assertThat(shouldBeEmpty).isEmpty();
        Assertions.assertThat(shouldContainOne).isPresent();
    }

    @Test
    void getServiceHistoryForServiceByMonths() {
        //Arrange
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        serviceEntity.setId(serviceRepository.save(serviceEntity));
        DailyStatusAggregationForServiceEntity aggregation = SampleData.getRandomizedDailyStatusAggregationForService(serviceEntity);
        aggregation.setAggregation_date(LocalDate.now().minusMonths(3));

        recordRepository.saveAggregatedRecords(aggregation);
        UUID serviceId = serviceEntity.getId();
        //Act
        List<DailyStatusAggregationForServiceEntity> shouldBeEmpty =
                recordRepository.getServiceHistoryForNumberOfMonths(serviceId, 1);
        List<DailyStatusAggregationForServiceEntity> shouldContainOne =
                recordRepository.getServiceHistoryForNumberOfMonths(serviceId, 4);
        //Assert
        Assertions.assertThat(shouldBeEmpty).isEmpty();
        Assertions.assertThat(shouldContainOne.size()).isEqualTo(1);
    }

    @Test
    void saveAggregatedRecords() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        DailyStatusAggregationForServiceEntity aggregation = SampleData.getRandomizedDailyStatusAggregationForService(service);
        //Act
        UUID aggregationId = recordRepository.saveAggregatedRecords(aggregation);
        aggregation.setId(aggregationId);
        List<DailyStatusAggregationForServiceEntity> retrievedAggregatedRecords = recordRepository.getServiceHistoryForNumberOfDays(1, serviceId);
        //Assert
        Assertions.assertThat(retrievedAggregatedRecords.size()).isEqualTo(1);
        Assertions.assertThat(retrievedAggregatedRecords).contains(aggregation);
    }

    @Test
    void getLatestRecord2() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = SampleData.getRandomizedRecordEntity();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime fiveDaysBack = now.minusHours(now.getHour()).minusDays(5);
        record.setCreated_at(fiveDaysBack);
        record.setServiceId(service.getId());
        record.setId(TestUtil.saveRecordBackInTime(record,dbContext));
        //Act
        List<RecordEntity> retrievedRecord = recordRepository.getRecordsOlderThan(2);
        record.setCreated_at(retrievedRecord.get(0).getCreated_at());
        //Assert
        List<RecordEntity> allRetrievedRecord = recordRepository.getRecordsOlderThan(5);
        Assertions.assertThat(allRetrievedRecord).containsAll(retrievedRecord);
        Assertions.assertThat(retrievedRecord.get(0).getCreated_at()).isEqualTo(record.getCreated_at());
        Assertions.assertThat(retrievedRecord.get(0).getServiceId()).isEqualTo(service.getId());
        Assertions.assertThat(retrievedRecord.get(0).getId()).isEqualTo(record.getId());
    }

    @Test
    void getRecordHistory() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        List<RecordEntity> records = SampleData.getRandomizedRecordEntitiesForService(service);
        records.forEach(record -> record.setId(recordRepository.save(record)));
        //Act
        List<RecordEntity> retrievedRecords = recordRepository.getRecordHistory(serviceId, records.size());
        //Assert
        Assertions.assertThat(retrievedRecords.size()).isEqualTo(records.size());
        Assertions.assertThat(retrievedRecords).hasSameElementsAs(retrievedRecords);
    }

    @Test
    void deleteRecordsOlderThen() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = SampleData.getRandomizedRecordEntity();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime fiveDaysBack = now.minusHours(now.getHour()).minusDays(5);
        record.setCreated_at(fiveDaysBack);
        record.setServiceId(service.getId());
        record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        //Act
        List<RecordEntity> retrievedRecordsBefore = recordRepository.getRecordsOlderThan(5);
        recordRepository.deleteRecordsOlderThen(5);
        //Assert
        List<RecordEntity> retrievedRecordsAfter = recordRepository.getRecordsOlderThan(5);
        Assertions.assertThat(retrievedRecordsBefore).isNotEmpty();
        Assertions.assertThat(retrievedRecordsAfter).isEmpty();
    }

    @Test
    void getAllRecordsFromYesterday(){
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        List<RecordEntity> records = SampleData.getRandomizedRecordEntitiesForService(service);
        records.forEach(record -> {
            int min = 1;
            int max = 5;
            ZonedDateTime now = ZonedDateTime.now();
            int numberOfDays = ThreadLocalRandom.current().nextInt(min, max + 1);
            ZonedDateTime daysBack = now.minusHours(now.getHour()).minusDays(numberOfDays);
            record.setCreated_at(daysBack);
            record.setServiceId(service.getId());
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });
        List<RecordEntity> retrievedRecordsBefore = recordRepository.getRecordsOlderThan(1);
        List<RecordEntity> retrievedRecordsOneDayBack = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime oneDaysBack = now.minusHours(now.getHour()).minusDays(1);
        retrievedRecordsBefore.forEach(recordEntity -> {
            if (recordEntity.getCreated_at().equals(oneDaysBack)){
                retrievedRecordsOneDayBack.add(recordEntity);
            }
        });
        //Act
        List<RecordEntity> retrievedRecordsAfter = recordRepository.getAllRecordsFromYesterday();
        //Assert
        Assertions.assertThat(retrievedRecordsBefore).isNotEmpty();
        Assertions.assertThat(retrievedRecordsAfter).containsAll(retrievedRecordsOneDayBack);
    }

    @Test
    void deleteRecords(){
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        List<RecordEntity> records = SampleData.getRandomizedRecordEntitiesForService(service);
        records.forEach(record -> {
            int min = 2;
            int max = 10;
            ZonedDateTime now = ZonedDateTime.now();
            int numberOfDays = ThreadLocalRandom.current().nextInt(min, max + 1);
            ZonedDateTime daysBack = now.minusHours(now.getHour()).minusDays(numberOfDays);
            record.setCreated_at(daysBack);
            record.setServiceId(service.getId());
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });
        List<RecordEntity> retrievedRecordsBefore = recordRepository.getRecordsOlderThan(2);
        //Act
        recordRepository.deleteRecords(retrievedRecordsBefore);
        //Assert
        List<RecordEntity> retrievedRecordsAfter = recordRepository.getRecordsOlderThan(2);
        Assertions.assertThat(retrievedRecordsBefore).isNotEmpty();
        Assertions.assertThat(retrievedRecordsAfter).isEmpty();
    }

    @Test
    void deleteRecordsOlderThen48hours(){
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        List<RecordEntity> records = SampleData.getRandomizedRecordEntitiesForService(service);
        records.forEach(record -> {
            int min = 2;
            int max = 5;
            ZonedDateTime now = ZonedDateTime.now();
            int numberOfDays = ThreadLocalRandom.current().nextInt(min, max + 1);
            ZonedDateTime fiveDaysBack = now.minusHours(now.getHour()).minusDays(numberOfDays);
            record.setCreated_at(fiveDaysBack);
            record.setServiceId(service.getId());
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });
        List<RecordEntity> retrievedRecordsBefore = recordRepository.getRecordsOlderThan(2);
        //Act
        recordRepository.deleteRecordsOlderThan48hours();
        //Assert
        List<RecordEntity> retrievedRecordsAfter = recordRepository.getRecordsOlderThan(2);
        Assertions.assertThat(retrievedRecordsBefore).isNotEmpty();
        Assertions.assertThat(retrievedRecordsAfter).isEmpty();
    }
}