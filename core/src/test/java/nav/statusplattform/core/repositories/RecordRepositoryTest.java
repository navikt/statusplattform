package nav.statusplattform.core.repositories;


import nav.statusplattform.core.entities.RecordDeltaEntity;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.ZonedDateTime;
import java.util.*;
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
       RecordEntity record = getRandomizedRecordEntity();
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
         Optional<RecordEntity>retrievedRecordBefore = recordRepository.getLatestRecord(serviceId);
         //Act
         recordRepository.saveNewStatusDiff(record);
         Optional<RecordDeltaEntity> retrievedRecordDiff = recordRepository.getLatestRecordDiff(serviceId);
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
        recordRepository.saveNewStatusDiff(record);
        Optional<RecordDeltaEntity>retrievedRecordDiffBefore = recordRepository.getLatestRecordDiff(serviceId);
        //Act
        retrievedRecordDiffBefore.ifPresent(recordRepository::setOldStatusDiffInactive);
        Optional<RecordDeltaEntity> retrievedRecordDiffAfter = recordRepository.getLatestRecordDiff(serviceId);
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
        recordRepository.saveNewStatusDiff(record);
        Optional<RecordDeltaEntity>retrievedRecordDiffBefore = recordRepository.getLatestRecordDiff(serviceId);
        //Act
        retrievedRecordDiffBefore.ifPresent(recordRepository::increaseCountOnStatusDiff);
        Optional<RecordDeltaEntity> retrievedRecordDiffAfter = recordRepository.getLatestRecordDiff(serviceId);
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
        RecordEntity record = getRandomizedRecordEntity();
        record.setServiceId(service.getId());
        record.setId(recordRepository.save(record));
        //Act
        Optional<RecordEntity> retrievedRecord = recordRepository.getLatestRecord(service.getId());
        record.setCreated_at(retrievedRecord.orElseThrow().getCreated_at());
        //Assert
        Assertions.assertThat(retrievedRecord.get()).isEqualTo(record);
    }

    @Test
    void getLatestRecord2() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = getRandomizedRecordEntity();
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
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 12);
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
        RecordEntity record = getRandomizedRecordEntity();
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
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 12);
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
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 12);
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
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 12);
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

    private RecordEntity getRandomizedRecordEntity() {
        return new RecordEntity()
                .setCreated_at(ZonedDateTime.now())
                .setStatus(SampleData.getRandomServiceStatus())
                .setResponsetime(SampleData.getRandomResponseTime());
    }

    private List<RecordEntity> generateRandomizedRecordEntities(ServiceEntity serviceEntity, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("argument amount refers to size of list, must be either non-zero or positive integer.");
        }
        List<RecordEntity> records = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            records.add(SampleData.getRandomizedRecordEntityForService(serviceEntity));
        }
        return records;
    }

}
