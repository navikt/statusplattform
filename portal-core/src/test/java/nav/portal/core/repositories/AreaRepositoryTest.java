package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import org.assertj.core.api.Assert;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.*;

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
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

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
        //TODO denne
        //Arrange
        List<AreaEntity> areas = sampleData.getNonEmptyListOfAreaEntity(2);

        UUID uuid = areaRepository.save(areas.get(0));
        areas.forEach(area -> area.setId(uuid));

        AreaEntity before = areaRepository.retrieveOne(uuid).getKey();
        //Act
        areaRepository.update(areas.get(1));
        AreaEntity after = areaRepository.retrieveOne(uuid).getKey();
        //Assert
        Assertions.assertThat(before).isEqualTo(areas.get(0));
        Assertions.assertThat(after).isEqualTo(areas.get(1));
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
       //Arrange
       AreaEntity area = sampleData.getRandomizedAreaEntity();
       UUID areaId = areaRepository.save(area);

       ServiceEntity service = sampleData.getRandomizedServiceEntity();
       UUID serviceId = serviceRepository.save(service);
       service.setId(serviceId);
       //Act
       areaRepository.addServiceToArea(areaId, serviceId);
       Map.Entry<AreaEntity,List<ServiceEntity>> retrievedArea = areaRepository.retrieveOne(areaId);
       //Assert
       Assertions.assertThat(retrievedArea.getValue()).containsExactly(service);
    }

    @Test
    void removeServiceFromArea() {
        //Arrange
        AreaEntity area = sampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);

        ServiceEntity service = sampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        areaRepository.addServiceToArea(areaId, serviceId);
        Map.Entry<AreaEntity, List<ServiceEntity>> before = areaRepository.retrieveOne(areaId);
        //Act
        areaRepository.removeServiceFromArea(areaId, serviceId);
        Map.Entry<AreaEntity, List<ServiceEntity>> after = areaRepository.retrieveOne(areaId);
        //Assert
        Assertions.assertThat(before.getValue()).containsExactly(service);
        Assertions.assertThat(after.getValue()).isEmpty();
    }


    @Test
    void retriveAllShallow() {
        //TODO denne
        //Arrange
        List<AreaEntity> areas = sampleData.getRandomLengthNonEmptyListOfAreaEntity();
        areas.forEach(area -> area.setId(areaRepository.save(area)));

        //Act
        List<AreaEntity>allAreas = areaRepository.retriveAllShallow();
        //Assert
        Assertions.assertThat(areas.size()).isEqualTo(allAreas.size());
        Assertions.assertThat(areas.containsAll(allAreas));
    }

    @Test
    void retrieveAll() {
        //Arrange
        AreaEntity area = sampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        area.setId(areaId);
        ServiceEntity service1 = sampleData.getRandomizedServiceEntity();
        ServiceEntity service2 = sampleData.getRandomizedServiceEntity();
        UUID service1Id = serviceRepository.save(service1);
        UUID service2Id = serviceRepository.save(service2);
        service1.setId(service1Id);
        service2.setId(service2Id);
        areaRepository.addServiceToArea(areaId, service1Id);
        areaRepository.addServiceToArea(areaId, service2Id);
        //Act
        Map<AreaEntity, List<ServiceEntity>> retrievedAll = areaRepository.retrieveAll();
        Map.Entry<AreaEntity, List<ServiceEntity>> retrievedArea = areaRepository.retrieveOne(areaId);
        List<ServiceEntity>services = retrievedArea.setValue(retrievedArea.getValue());
        //Assert
        Assertions.assertThat(retrievedAll.containsKey(area));
        Assertions.assertThat(retrievedAll.containsValue(services));
    }
}