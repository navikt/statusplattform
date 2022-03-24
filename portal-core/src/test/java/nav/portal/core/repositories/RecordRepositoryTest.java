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
    private SampleData sampleData = new SampleData();

    private DataSource dataSource = TestDataSource.create();

    private DbContext dbContext = new DbContext();
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
        ServiceEntity service = sampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = sampleData.getRandomizedRecordEntity();
        record.setServiceId(service.getId());
        //Act
        record.setId(recordRepository.save(record));
        Optional<RecordEntity>retrievedEntity = recordRepository.getLatestRecord(serviceId);
        record.setCreated_at(retrievedEntity.get().getCreated_at());
        //Assert
        Assertions.assertThat(retrievedEntity.get()).isEqualTo(record);
     }

    @Test
    void getLatestRecord() {
        //Arrange
        ServiceEntity service = sampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = sampleData.getRandomizedRecordEntity();
        record.setServiceId(service.getId());
        record.setId(recordRepository.save(record));
        //Act
        Optional<RecordEntity> retrievedRecord = recordRepository.getLatestRecord(service.getId());
        record.setCreated_at(retrievedRecord.get().getCreated_at());
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
        record.setId(TestUtil.saveRecordBackInTime(record,dbContext));//Her må testutil.save meto
        //Act
        List<RecordEntity> retrievedRecord = recordRepository.getRecordsOlderThan(2);
        record.setCreated_at(retrievedRecord.get(0).getCreated_at());
        //Assert

    }

}