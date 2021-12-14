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

import java.util.*;
import java.util.stream.Collectors;

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


    }
}