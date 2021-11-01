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
import java.util.stream.Collectors;

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
        area.setId(uuid);
        AreaEntity retrievedEntity = areaRepository.retrieveOne(uuid).getKey();
        //Assert
        Assertions.assertThat(retrievedEntity).isEqualTo(area);
    }

    @Test
    void retrieveOne() {
        //Arrange
        AreaEntity area = sampleData.getRandomizedAreaEntity();
        UUID uuid = areaRepository.save(area);
        area.setId(uuid);
        //Act
        Map.Entry<AreaEntity, List<ServiceEntity>> retrievedArea = areaRepository.retrieveOne(uuid);
        //Assert
        Assertions.assertThat(retrievedArea.getValue().size()).isEqualTo(0);
        Assertions.assertThat(retrievedArea.getKey()).isEqualTo(area);
    }

    @Test
    void update() {
    }


    @Test
    void deleteArea() {
        //TODO denne
        //Arrange - Lag area
        List<AreaEntity> areas = sampleData.getRandomLengthNonEmptyListOfAreaEntity();
        for(AreaEntity area :areas){
             area.setId(areaRepository.save(area));
        }
        AreaEntity areaToBeDeleted = areas.get(0);

        //Act
        List<AreaEntity> retrievedbeforeDelete = areaRepository.retriveAllShallow();
        boolean isDeleted = areaRepository.deleteArea(areaToBeDeleted.getId());
        List<AreaEntity> retrievedAreasAfterDelete = areaRepository.retriveAllShallow();
        retrievedbeforeDelete.removeAll(retrievedAreasAfterDelete);
        //Assert
        Assertions.assertThat(isDeleted).isTrue();
        Assertions.assertThat(retrievedbeforeDelete.size()).isEqualTo(1);
        Assertions.assertThat(retrievedbeforeDelete.get(0)).isEqualTo(areaToBeDeleted);

    }

    @Test
    void addServiceToArea() {
    }

    @Test
    void removeServiceFromArea() {
    }


    @Test
    void retriveAllShallow() {
        //Arrange
        //Act
        areaRepository.retriveAllShallow();
        //Assert

        //TODO denne
    }

    @Test
    void retrieveAll() {
    }
}