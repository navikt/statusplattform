package no.nav.portal.rest.api.v3.controllers;


import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.entities.SubAreaEntity;
import no.portal.web.generated.api.IdContainerDto;
import no.portal.web.generated.api.SubAreaDto;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import nav.portal.core.entities.AreaEntity;
import no.portal.web.generated.api.AreaDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

class AreaControllerTest {

    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final AreaController areaController = new AreaController(dbContext);
    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final SubAreaRepository subAreaRepository = new SubAreaRepository(dbContext);
    private final DashboardRepository dashboardRepository = new DashboardRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    @Test
    void getAllAreas() {
        //Arrange
        List<AreaDto> areaDtos = SampleDataDto.getRandomLengthListOfAreaDto();
        List <UUID> areaDtoIds = new ArrayList<>();
        areaDtos.forEach(areaDto -> {
            IdContainerDto areaIdContainerDto  = areaController.newArea(areaDto);
            areaDto.setId(areaIdContainerDto.getId());
            areaDtoIds.add(areaIdContainerDto.getId());
        });
        //Act
        List<AreaDto> retrievedAreasDtos = areaController.getAllAreas();
        List <UUID> retrievedAreaDtoIds = new ArrayList<>();
        retrievedAreasDtos.forEach(areaDto -> retrievedAreaDtoIds.add(areaDto.getId()));
        //Assert
        Assertions.assertThat(retrievedAreasDtos.size()).isEqualTo(areaDtos.size());
        Assertions.assertThat(retrievedAreaDtoIds).containsExactlyInAnyOrderElementsOf(areaDtoIds);
    }

    @Test
    void newArea() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        AreaDto areaDto = EntityDtoMappers.toAreaDtoShallow(area);
        //Act
        IdContainerDto areaIdContainerDto = areaController.newArea(areaDto);
        Map.Entry<AreaEntity, List<ServiceEntity>> retrievedNewArea = areaRepository.retrieveOne(areaIdContainerDto.getId());
        //Assert
        Assertions.assertThat(retrievedNewArea.getKey().getName()).isEqualTo(area.getName());
        Assertions.assertThat(retrievedNewArea.getKey().getDescription()).isEqualTo(area.getDescription());
        Assertions.assertThat(retrievedNewArea.getKey().getIcon()).isEqualTo(area.getIcon());
        Assertions.assertThat(retrievedNewArea.getValue()).isEmpty();
    }

    @Test
    void updateArea() {
        //Arrange
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(2);
        List<AreaDto> areaDtos = EntityDtoMappers.toAreaDtoShallow(areas);

        UUID areaId1 = areaRepository.save(areas.get(0));

        areas.forEach(area -> area.setId(areaId1));

        AreaEntity before = areaRepository.retrieveOne(areaId1).getKey();
        //Act
        areaController.updateArea(areaId1, areaDtos.get(1));
        //Assert
        AreaEntity after = areaRepository.retrieveOne(areaId1).getKey();
        Assertions.assertThat(after.getId()).isEqualTo(before.getId());
        Assertions.assertThat(after).isNotEqualTo(before);
    }

    @Test
    void deleteArea() {
        //Arrange
        int NumberOfAreas = 2;
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(NumberOfAreas);
        /*for(AreaEntity area :areas){
            area.setId(areaRepository.save(area));
        }*/

        // Alternativ 2 med bruk av for-each loop og lambda
        //areas.forEach(area -> area.setId(areaRepository.save(area)));

        // Alternativ 3 med bruk av stream og lambda
        List<UUID> areas_ids =  areas.stream()
                .map(areaRepository::save)
                .collect(Collectors.toList());

        AreaEntity areaToBeDeleted = areas.get(0);
        //Act
        List<AreaEntity> retrievedBeforeDelete = areaRepository.retriveAllShallow();
        areaController.deleteArea(areas_ids.get(0));
        List<AreaEntity> retrievedAreasAfterDelete = areaRepository.retriveAllShallow();
        //Assert
        Assertions.assertThat(areas.size()).isEqualTo(2);
        Assertions.assertThat(retrievedBeforeDelete.size()).isEqualTo(2);
        Assertions.assertThat(retrievedAreasAfterDelete.size()).isEqualTo(1);
        Assertions.assertThat(retrievedAreasAfterDelete).doesNotContain(areaToBeDeleted);
    }


    @Test
    void getAreas() {
        //Arrange
        String dashboardName = SampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(3);
        List<UUID> areaIds = areas.stream().map(areaRepository::save).collect(Collectors.toList());
        List<AreaDto> beforeDto = EntityDtoMappers.toAreaDtoDeep(areaRepository.retrieveAll());
        dashboardRepository.settAreasOnDashboard(dashboardId,areaIds);
        //Act
        List<AreaDto> afterDto = areaController.getAreas(dashboardId);
        //Assert
        Assertions.assertThat(afterDto.size()).isEqualTo(3);
        Assertions.assertThat(afterDto.size()).isEqualTo(beforeDto.size());
        Assertions.assertThat(afterDto.containsAll(beforeDto)).isTrue();
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
        areaController.addServiceToArea(areaId, serviceId );
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
        areaController.addServiceToArea(areaId, serviceId);
        Map.Entry<AreaEntity, List<ServiceEntity>> before = areaRepository.retrieveOne(areaId);
        //Act
        areaRepository.removeServiceFromArea(areaId, serviceId);
        Map.Entry<AreaEntity, List<ServiceEntity>> after = areaRepository.retrieveOne(areaId);
        //Assert
        Assertions.assertThat(before.getValue()).containsExactly(service);
        Assertions.assertThat(after.getValue()).isEmpty();
    }

    @Test
    void getAllSubAreas() {
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
        List<SubAreaDto> subAreasDtos = areaController.getAllSubAreas();
        //Assert
        Assertions.assertThat(subAreasDtos.get(0).getId()).isEqualTo(subAreaId);
    }

    @Test
    void newSubArea() {
        //Arrange
        SubAreaEntity subArea = SampleData.getRandomizedSubAreaEntity();
        SubAreaDto subAreaDto = EntityDtoMappers.toSubAreaDtoShallow(subArea);
        //Act
        IdContainerDto subAreaIdContainerDto = areaController.newSubArea(subAreaDto);
        Map.Entry<SubAreaEntity, List<ServiceEntity>> retrievedNewSubArea = subAreaRepository.retrieveOne(subAreaIdContainerDto.getId());
        //Assert
        Assertions.assertThat(retrievedNewSubArea.getKey().getName()).isEqualTo(subArea.getName());
        Assertions.assertThat(retrievedNewSubArea.getValue()).isEmpty();
    }

}