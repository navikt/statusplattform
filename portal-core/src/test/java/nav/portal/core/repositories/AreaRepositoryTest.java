package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AreaRepositoryTest {

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
        connection.close();
    }

    private final AreaRepository areaRepository = new AreaRepository(dbContext);

    @Test
    void save() {
        //Arrange
        AreaEntity area = sampleData.getRandomizedAreaEntity();
        //Act
        UUID uuid = areaRepository.save(area);
        //Assert
        Assertions.assertThat(uuid).isNotNull();
    }

    @Test
    void retrieveOne() {
        //Arrange
        AreaEntity area = sampleData.getRandomizedAreaEntity();
        UUID uuid = areaRepository.save(area);
        area.setId(uuid);
        //Act
        Map.Entry<AreaEntity, List<ServiceEntity>> retreavedArea = areaRepository.retrieveOne(uuid);
        //Assert
        Assertions.assertThat(retreavedArea.getValue().size()).isEqualTo(0);
        Assertions.assertThat(retreavedArea.getKey()).isEqualTo(area);
    }

    @Test
    void update() {
    }


    @Test
    void deleteArea() {
    }

    @Test
    void addServiceToArea() {
    }

    @Test
    void removeServiceFromArea() {
    }


    @Test
    void retriveAllShallow() {
    }

    @Test
    void retrieveAll() {
    }
}