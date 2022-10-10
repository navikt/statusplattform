package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.entities.SubAreaEntity;
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
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final SubAreaRepository subAreaRepository = new SubAreaRepository(dbContext);
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
    void updateArea() {
        //TODO denne
        //Arrange

        String updatedAreaName = "A_name_you_wont_find_in_sampldata";
        AreaEntity areaBeforeUpdate = SampleData.getRandomizedAreaEntity();
        areaBeforeUpdate.setId(areaRepository.save(areaBeforeUpdate));
        AreaEntity areaAfterUpdate =
                new AreaEntity(areaBeforeUpdate.getId()
                ,updatedAreaName
                ,areaBeforeUpdate.getDescription()
                ,areaBeforeUpdate.getIcon());

        //Act
        areaRepository.updateArea(areaAfterUpdate);

        //Assert
        Assertions.assertThat(areaBeforeUpdate.getName()).isNotEqualToIgnoringCase(areaAfterUpdate.getName());
        Assertions.assertThat(areaAfterUpdate.getName()).isEqualTo(updatedAreaName);
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
    void addSubAreaToArea() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);

        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        UUID subAreaId = subAreaRepository.save(subArea);
        subArea.setId(subAreaId);
        //Act
        areaRepository.addSubAreaToArea(areaId, subAreaId);
        List<SubAreaEntity> retrievedArea = areaRepository.getSubAreasOnArea(areaId);
        //Assert
        Assertions.assertThat(retrievedArea.contains(subArea)).isTrue();
    }

    @Test
    void addServiceToAreas() {
        //Arrange
        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();
        List<UUID> areaIds = new ArrayList<>();
        areas.forEach(area -> {
            area.setId(areaRepository.save(area));
            areaIds.add(area.getId());
        });
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        Map<AreaEntity, List<ServiceEntity>> retrievedAreasBefore = areaRepository.retrieveAll();
        //Act
        areaRepository.addServiceToAreas(areaIds, serviceId);
        Map<AreaEntity, List<ServiceEntity>> retrievedAreasAfter = areaRepository.retrieveAll();
        //Assert
        Assertions.assertThat(retrievedAreasBefore.keySet()).containsAll(areas);
        Assertions.assertThat(retrievedAreasAfter.keySet()).containsAll(areas);
        Assertions.assertThat(retrievedAreasBefore.values()).doesNotContain(List.of(service));
        Assertions.assertThat(retrievedAreasAfter.values()).contains(Collections.singletonList(service));
    }

    @Test
    void setServicesOnArea() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
        List<UUID> serviceIds = new ArrayList<>();
        services.forEach(s ->
        {
            s.setId(serviceRepository.save(s));
            serviceIds.add(s.getId());
        });
        //Act
        areaRepository.setServicesOnArea(areaId, serviceIds);
        Map.Entry<AreaEntity, List<ServiceEntity>> retrievedArea  = areaRepository.retrieveOne(areaId);
        //Assert
        Assertions.assertThat(retrievedArea.getKey().getId()).isEqualTo(areaId);
        Assertions.assertThat(retrievedArea.getValue()).containsAll(services);
    }

    @Test
    void removeServiceFromAllAreas(){
        //Arrange
        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();
        areas.forEach(area -> {
            area.setId(areaRepository.save(area));
        });
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(1);
        List<UUID> serviceIds = new ArrayList<>();
        services.forEach(service -> {
            service.setId(serviceRepository.save(service));
            serviceIds.add(service.getId());
        });
        ServiceEntity service = services.get(0);
        UUID serviceToBeDeleted = service.getId();

        areas.forEach(area -> {
            areaRepository.setServicesOnArea(area.getId(), serviceIds);
        });
        Map<AreaEntity, List<ServiceEntity>> retrievedAllAreasServicesBefore  = areaRepository.retrieveAll();
        //Act
        areaRepository.removeServiceFromAllAreas(serviceToBeDeleted);
        Map<AreaEntity, List<ServiceEntity>> retrievedAllAreasServicesAfter  = areaRepository.retrieveAll();
        //Assert
        Assertions.assertThat(retrievedAllAreasServicesBefore.keySet()).containsAll(areas);
        Assertions.assertThat(retrievedAllAreasServicesBefore.values()).contains(services);
        Assertions.assertThat(retrievedAllAreasServicesBefore.values()).contains(Collections.singletonList(service));

        Assertions.assertThat(retrievedAllAreasServicesAfter.keySet()).containsAll(areas);
        Assertions.assertThat(retrievedAllAreasServicesAfter.values()).doesNotContain(List.of(service));
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
        Assertions.assertThat(retrievedAll.get(area)).containsExactlyInAnyOrderElementsOf(retrievedServices);
    }

     @Test
    void getSubAreasOnArea() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        List<SubAreaEntity> subAreas = SampleData.getRandomLengthListOfSubAreaEntity();
        List<UUID> subAreaIds = new ArrayList<>();
        subAreas.forEach(subArea -> {
            subArea.setId(subAreaRepository.save(subArea));
            subAreaIds.add(subArea.getId());
        });
        areaRepository.addSubAreaToArea(areaId, subAreaIds);
        //Act
        List<SubAreaEntity> retrievedSubAreas = areaRepository.getSubAreasOnArea(areaId);
        //Assert
        Assertions.assertThat(retrievedSubAreas.size()).isEqualTo(subAreas.size());
        Assertions.assertThat(retrievedSubAreas.containsAll(subAreas)).isTrue();
     }

}