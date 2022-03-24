package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.*;

class AreaRepositoryTest {

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

    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

    @Test
    void save() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
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
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        area.setId(areaId);
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
        /*for(ServiceEntity service : services){
            service.setId(serviceRepository.save(service));
            areaRepository.addServiceToArea(areaId, service.getId());
        }*/

        services.forEach(s ->
        {
            s.setId(serviceRepository.save(s));
            areaRepository.addServiceToArea(areaId, s.getId());
        });

        //Act
        Map.Entry<AreaEntity, List<ServiceEntity>> retrievedArea = areaRepository.retrieveOne(areaId);
        //Assert
        List<ServiceEntity>retrievedServices = retrievedArea.getValue();
        Assertions.assertThat(retrievedServices.size()).isEqualTo(retrievedServices.size());
        Assertions.assertThat(retrievedServices).containsAll(services);
        Assertions.assertThat(retrievedArea.getKey()).isEqualTo(area);
    }


    @Test
    void update() {
        //TODO denne
        //Arrange
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(2);

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
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(3);

        /*for(AreaEntity area :areas){
             area.setId(areaRepository.save(area));
        }*/

        areas.forEach(area -> area.setId(areaRepository.save(area)));

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
       AreaEntity area = SampleData.getRandomizedAreaEntity();
       UUID areaId = areaRepository.save(area);

       ServiceEntity service = SampleData.getRandomizedServiceEntity();
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
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);

        ServiceEntity service = SampleData.getRandomizedServiceEntity();
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
        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();
        areas.forEach(area -> area.setId(areaRepository.save(area)));

        //Act
        List<AreaEntity>allAreas = areaRepository.retriveAllShallow();
        //Assert
        Assertions.assertThat(allAreas.size()).isEqualTo(areas.size());
        Assertions.assertThat(allAreas).containsAll(areas);
    }

    @Test
    void retrieveAll() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        area.setId(areaId);

        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
        /*for(ServiceEntity service : services){
            service.setId(serviceRepository.save(service));
            areaRepository.addServiceToArea(areaId, service.getId());
        }*/

        services.forEach(s ->
        {
            s.setId(serviceRepository.save(s));
            areaRepository.addServiceToArea(areaId, s.getId());
        });


        //Act
        Map<AreaEntity, List<ServiceEntity>> retrievedAll = areaRepository.retrieveAll();
        Map.Entry<AreaEntity, List<ServiceEntity>> retrievedArea = areaRepository.retrieveOne(areaId);
        List<ServiceEntity>retrievedServices = retrievedArea.setValue(retrievedArea.getValue());
        //Assert
        Assertions.assertThat(retrievedAll).containsKey(area);
        Assertions.assertThat(retrievedAll).containsValue(retrievedServices);
    }
}