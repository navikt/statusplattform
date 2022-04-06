package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.entities.SubAreaEntity;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.*;
import javax.sql.DataSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

class SubAreaRepositoryTest {
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
    private final SubAreaRepository subAreaRepository = new SubAreaRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

    @Test
    void save() {
        //Arrange
        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        //Act
        UUID uuid = subAreaRepository.save(subArea);
        subArea.setId(uuid);
        SubAreaEntity retrievedEntity = subAreaRepository.retrieveOne(uuid).getKey();
        //Assert
        Assertions.assertThat(retrievedEntity).isEqualTo(subArea);
    }

    @Test
    void updateSubArea() {
        //Arrange
        List<SubAreaEntity> subAreas = SampleData.getNonEmptyListOfSubAreaEntity(2);

        UUID uuid = subAreaRepository.save(subAreas.get(0));
        subAreas.forEach(sub_area -> sub_area.setId(uuid));

        SubAreaEntity before = subAreaRepository.retrieveOne(uuid).getKey();
        //Act
        subAreaRepository.updateSubArea(subAreas.get(1));
        SubAreaEntity after = subAreaRepository.retrieveOne(uuid).getKey();
        //Assert
        Assertions.assertThat(before).isEqualTo(subAreas.get(0));
        Assertions.assertThat(after).isEqualTo(subAreas.get(1));
    }

    @Test
    void deleteSubArea() {
        //Arrange
        List<SubAreaEntity> subAreas = SampleData.getNonEmptyListOfSubAreaEntity(3);

        subAreas.forEach(subArea -> subArea.setId(subAreaRepository.save(subArea)));
        SubAreaEntity subAreaToBeDeleted = subAreas.get(0);

        //Act
        List<SubAreaEntity> retrievedbeforeDelete = subAreaRepository.retriveAllShallow();
        boolean isDeleted = subAreaRepository.deleteSubArea(subAreaToBeDeleted.getId());
        List<SubAreaEntity> retrievedSubAreasAfterDelete = subAreaRepository.retriveAllShallow();
        retrievedbeforeDelete.removeAll(retrievedSubAreasAfterDelete);
        //Assert
        Assertions.assertThat(isDeleted).isTrue();
        Assertions.assertThat(retrievedbeforeDelete.size()).isEqualTo(1);
        Assertions.assertThat(retrievedbeforeDelete.get(0)).isEqualTo(subAreaToBeDeleted);
    }

    @Test
    void getServicesOnSubArea() {
        //Arrange
        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        UUID subAreaId = subAreaRepository.save(subArea);
        subArea.setId(subAreaId);

        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(1);

        services.forEach(s ->
        {
            s.setId(serviceRepository.save(s));
            subAreaRepository.addServiceToSubArea(subAreaId, s.getId());
        });
        //Act
        List<ServiceEntity> allRetrieved = subAreaRepository.getServicesOnSubArea(subAreaId);
        //Assert
        Assertions.assertThat(allRetrieved.size()).isEqualTo(services.size());
        Assertions.assertThat(allRetrieved).containsAll(services);
    }

    @Test
    void addServiceToSubArea() {
        //Arrange
        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        UUID subAreaId = subAreaRepository.save(subArea);
        subArea.setId(subAreaId);

        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);

        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        area.setId(areaId);

        //Act
        areaRepository.addSubAreaToArea(areaId, subAreaId);
        subAreaRepository.addServiceToSubArea(subAreaId, serviceId);
        Map.Entry<SubAreaEntity,List<ServiceEntity>> retrievedSubArea = subAreaRepository.retrieveOne(subAreaId);
        //Assert
        Assertions.assertThat(retrievedSubArea.getValue()).containsExactly(service);
    }
    /*
    @Test
    void setServicesOnSubArea() {

        //Arrange
        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        UUID subAreaId = subAreaRepository.save(subArea);
        subArea.setId(subAreaId);

        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);

        services.forEach(s ->
        {
            s.setId(serviceRepository.save(s));
            subAreaRepository.addServiceToSubArea(subAreaId, s.getId());
        });
        //Act
        subAreaRepository.setServicesOnSubArea(subAreaId, services);
        List<ServiceEntity> retrieved = subAreaRepository.getServicesOnSubArea(subAreaId);
        //Assert
        Assertions.assertThat(retrieved.size()).isEqualTo(services.size());
        Assertions.assertThat(retrieved).containsAll(services);
    }
    */
    @Test
    void removeServiceFromAllSubAreas() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);

        List<SubAreaEntity> subAreas = SampleData.getNonEmptyListOfSubAreaEntity(3);

        subAreas.forEach(subArea -> subArea.setId(subAreaRepository.save(subArea)));

        List<SubAreaEntity> before = subAreaRepository.getAreasContainingService(serviceId);

        //Act
        subAreaRepository.removeServiceFromAllSubAreas(serviceId);
        //Assert
        Assertions.assertThat(before).containsAll(subAreas);
        Assertions.assertThat(subAreaRepository.getAreasContainingService(serviceId)).isEmpty();
    }

    @Test
    void removeServiceFromSubArea() {
        //Arrange
        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        UUID subAreaId = subAreaRepository.save(subArea);
        subArea.setId(subAreaId);

        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        subAreaRepository.addServiceToSubArea(subAreaId, serviceId);
        Map.Entry<SubAreaEntity, List<ServiceEntity>> before = subAreaRepository.retrieveOne(subAreaId);
        //Act
        subAreaRepository.removeServiceFromSubArea(subAreaId, serviceId);
        Map.Entry<SubAreaEntity, List<ServiceEntity>> after = subAreaRepository.retrieveOne(subAreaId);
        //Assert
        Assertions.assertThat(before.getValue()).containsExactly(service);
        Assertions.assertThat(after.getValue()).isEmpty();
    }

    @Test
    void retrieveOne() {
        //Arrange
        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        UUID subAreaId = subAreaRepository.save(subArea);
        subArea.setId(subAreaId);
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(1);

        services.forEach(s ->
        {
            s.setId(serviceRepository.save(s));
            subAreaRepository.addServiceToSubArea(subAreaId, s.getId());
        });
        //Act
        Map.Entry<SubAreaEntity, List<ServiceEntity>> retrievedSubArea = subAreaRepository.retrieveOne(subAreaId);
        //Assert
        List<ServiceEntity> retrievedServices = retrievedSubArea.getValue();
        Assertions.assertThat(retrievedServices).containsAll(services);
        Assertions.assertThat(retrievedSubArea.getKey()).isEqualTo(subArea);
    }

    @Test
    void retriveAllShallow() {
        //Arrange
        List<SubAreaEntity> subAreas = SampleData.getRandomLengthListOfSubAreaEntity();
        subAreas.forEach(subArea -> subArea.setId(subAreaRepository.save(subArea)));

        //Act
        List<SubAreaEntity> allSubAreas = subAreaRepository.retriveAllShallow();
        //Assert
        Assertions.assertThat(allSubAreas.size()).isEqualTo(subAreas.size());
        Assertions.assertThat(allSubAreas).containsAll(subAreas);
    }

    @Test
    void retrieveAll() {
        //Arrange
        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        UUID subAreaId = subAreaRepository.save(subArea);
        subArea.setId(subAreaId);

        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(1);

        services.forEach(s ->
        {
            s.setId(serviceRepository.save(s));
            subAreaRepository.addServiceToSubArea(subAreaId, s.getId());
        });

        //Act
        Map<SubAreaEntity, List<ServiceEntity>> retrievedAll = subAreaRepository.retrieveAll();
        Map.Entry<SubAreaEntity, List<ServiceEntity>> retrievedSubArea = subAreaRepository.retrieveOne(subAreaId);
        List<ServiceEntity> retrievedServices = retrievedSubArea.setValue(retrievedSubArea.getValue());
        //Assert
        Assertions.assertThat(retrievedAll).containsKey(subArea);
        Assertions.assertThat(retrievedAll).containsValue(retrievedServices);
    }

    @Test
    void getAreasContainingService() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);

        List<SubAreaEntity> subAreas = SampleData.getNonEmptyListOfSubAreaEntity(1);

        subAreas.forEach(subArea ->
        {
            subArea.setId(subAreaRepository.save(subArea));
            subAreaRepository.addServiceToSubArea(subArea.getId(), serviceId);
        });

        //Act
        List<SubAreaEntity> retrievedAll = subAreaRepository.getAreasContainingService(serviceId);
        //Assert
        Assertions.assertThat(retrievedAll.size()).isEqualTo(subAreas.size());
        Assertions.assertThat(retrievedAll).containsAll(subAreas);
    }
}