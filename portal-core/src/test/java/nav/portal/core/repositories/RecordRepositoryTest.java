package nav.portal.core.repositories;


import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.entities.RecordEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.time.ZonedDateTime;
import java.util.*;

import java.util.UUID;

class RecordRepositoryTest {

    private final DataSource dataSource = TestDataSource.create();

    private final DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
    }

    @AfterEach
    void endConnection() {
        TestUtil.clearAllTableData(dbContext);
        connection.close();
    }

    private final RecordRepository recordRepository = new RecordRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

    @Test
    void name() {
    }

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

}