package no.nav.portal.rest.api.v3.controllers;


import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.entities.SubAreaEntity;
import no.portal.web.generated.api.*;
import org.actioncontroller.PathParam;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import nav.portal.core.entities.AreaEntity;
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

    private final DashboardController dashboardController = new DashboardController(dbContext);
    private final AreaController areaController = new AreaController(dbContext);
    private final ServiceController serviceController = new ServiceController(dbContext);
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
        ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        ServiceDto savedServiceDto = serviceController.newService(serviceDto);
        serviceDto.setId(savedServiceDto.getId());
        areaDtos.forEach(areaDto -> {
            IdContainerDto areaIdContainerDto  = areaController.newArea(areaDto);
            areaDto.setId(areaIdContainerDto.getId());
            areaDto.setServices(List.of(serviceDto));
            areaDtoIds.add(areaIdContainerDto.getId());
        });
        //Act
        List<AreaDto> retrievedAreasDtos = areaController.getAllAreas();
        List <UUID> retrievedAreaDtoIds = new ArrayList<>();
        retrievedAreasDtos.forEach(areaDto ->  retrievedAreaDtoIds.add(areaDto.getId()));
        //Assert
        Assertions.assertThat(retrievedAreasDtos.size()).isEqualTo(areaDtos.size());
        Assertions.assertThat(retrievedAreaDtoIds).containsExactlyInAnyOrderElementsOf(areaDtoIds);
     }

    @Test
    void newArea() {
        //Arrange
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        //Act
        IdContainerDto areaIdContainerDto = areaController.newArea(areaDto);
        areaDto.setId(areaIdContainerDto.getId());
        List<AreaDto> retrievedAreasDtos = areaController.getAllAreas();
        //Assert
        Assertions.assertThat(retrievedAreasDtos).contains(areaDto);
        Assertions.assertThat(retrievedAreasDtos.size()).isEqualTo(List.of(areaDto).size());
        Assertions.assertThat(retrievedAreasDtos.get(0).getId()).isEqualTo(areaDto.getId());
    }

    @Test
    void updateArea() {
        //Arrange
        List<AreaDto> areaDtos = SampleDataDto.getNonEmptyListOfAreaDto(2);

        IdContainerDto areaIdContainerDto1 = areaController.newArea(areaDtos.get(0));
        areaDtos.get(0).setId(areaIdContainerDto1.getId());

        IdContainerDto areaIdContainerDto2 = areaController.newArea(areaDtos.get(1));
        areaDtos.get(1).setId(areaIdContainerDto2.getId());

        List<AreaDto> areaDtosBefore = areaController.getAllAreas();

        //Act
        areaController.updateArea(areaDtos.get(0).getId(), areaDtos.get(1));
        List<AreaDto> areaDtosAfter = areaController.getAllAreas();

        //Assert
        Assertions.assertThat(areaDtosBefore.get(0).getName()).isNotEqualToIgnoringCase(areaDtosBefore.get(1).getName());
        Assertions.assertThat(areaDtosAfter.get(0).getName()).isEqualToIgnoringCase(areaDtosAfter.get(1).getName());

    }

    @Test
    void deleteArea() {
        //Arrange
        int NumberOfAreas = 2;
        List<AreaDto> areaDtos = SampleDataDto.getNonEmptyListOfAreaDto(NumberOfAreas);

        ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        ServiceDto savedServiceDto = serviceController.newService(serviceDto);
        serviceDto.setId(savedServiceDto.getId());
        areaDtos.forEach(areaDto -> {
            IdContainerDto areaIdContainerDto  = areaController.newArea(areaDto);
            areaDto.setId(areaIdContainerDto.getId());
            areaDto.setServices(List.of(serviceDto));
        });
        AreaDto areaToBeDeleted = areaDtos.get(0);

        //Act
        List<AreaDto> retrievedBeforeDelete = areaController.getAllAreas();
        areaController.deleteArea(areaDtos.get(0).getId());
        List<AreaDto> retrievedAreasAfterDelete = areaController.getAllAreas();

        //Assert
        Assertions.assertThat(areaDtos.size()).isEqualTo(2);
        Assertions.assertThat(retrievedBeforeDelete.size()).isEqualTo(2);
        Assertions.assertThat(retrievedAreasAfterDelete.size()).isEqualTo(1);
        Assertions.assertThat(retrievedAreasAfterDelete).doesNotContain(areaToBeDeleted);
    }

    @Test
    void getAreas() {
        //Arrange
        List<AreaDto> areaDtos = SampleDataDto.getRandomLengthListOfAreaDto();
        areaDtos.forEach(areaDto -> {
            IdContainerDto areaIdContainerDto  = areaController.newArea(areaDto);
            areaDto.setId(areaIdContainerDto.getId());
        });
        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(areaDtos);
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());

        List<AreaDto> beforeDtos = areaController.getAllAreas();
        //Act
        List<AreaDto> afterDtos = areaController.getAreas(dashboardDto.getId());
        //Assert
        Assertions.assertThat(afterDtos.size()).isEqualTo(areaDtos.size());
        Assertions.assertThat(afterDtos.size()).isEqualTo(beforeDtos.size());
        Assertions.assertThat(afterDtos.containsAll(beforeDtos)).isTrue();
    }

    @Test
    void addServiceToArea() {
        //Arrange
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto = areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());

        ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        ServiceDto savedServiceDto = serviceController.newService(serviceDto);
        serviceDto.setId(savedServiceDto.getId());

        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());

        List<AreaDto> beforeDtos = areaController.getAreas(dashboardDto.getId());
        List<ServiceDto> serviceDtosBefore = beforeDtos.get(0).getServices();
        //Act
        areaController.addServiceToArea(areaDto.getId(), serviceDto.getId());
        //Assert
        List<AreaDto> afterDtos = areaController.getAreas(dashboardDto.getId());
        List<ServiceDto> serviceDtosAfter = afterDtos.get(0).getServices();
        Assertions.assertThat(beforeDtos.get(0).getId()).isEqualTo(afterDtos.get(0).getId());
        Assertions.assertThat((serviceDtosBefore).isEmpty()).isTrue();
        Assertions.assertThat(serviceDtosAfter).contains(serviceDto);
    }

    @Test
    void removeServiceFromArea() {
        //Arrange
        List<ServiceDto> serviceDtos = SampleDataDto.getNonEmptyListOfServiceDto(2);
        serviceDtos.forEach(serviceDto -> {
            ServiceDto savedServiceDto = serviceController.newService(serviceDto);
            serviceDto.setId(savedServiceDto.getId());
        });

        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto = areaController.newArea(areaDto);


        areaDto.setId(idContainerDto.getId());

        serviceDtos.forEach(serviceDto -> areaController.addServiceToArea(areaDto.getId(), serviceDto.getId()));
        areaDto.setServices(serviceDtos);
        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());

        List <AreaDto> areaDtoBefore = areaController.getAreas(dashboardDto.getId());
        List<ServiceDto> serviceDtosBefore = areaDtoBefore.get(0).getServices();
        ServiceDto toBeDeleted = serviceDtosBefore.get(0);
        //Act
        areaController.removeServiceFromArea(areaDto.getId(),toBeDeleted.getId());
        List <AreaDto> areaDtoAfter = areaController.getAreas(dashboardDto.getId());
        List<ServiceDto> serviceDtosAfter = areaDtoAfter.get(0).getServices();
        //Assert
        Assertions.assertThat(serviceDtosBefore.size()).isEqualTo(2);
        Assertions.assertThat(serviceDtosAfter.size()).isEqualTo(1);
        Assertions.assertThat(serviceDtosBefore).contains(toBeDeleted);
        Assertions.assertThat(serviceDtosAfter).doesNotContain(toBeDeleted);
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
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto = areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());

        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());

        List <AreaDto> areaDtosBefore = areaController.getAreas(dashboardDto.getId());
        List <SubAreaDto> subAreaDtosBefore = areaController.getAllSubAreas();

        //Act
        SubAreaDto subAreaDto = SampleDataDto.getRandomizedSubAreaDto();
        IdContainerDto subAreaIdContainerDto = areaController.newSubArea(subAreaDto);
        areaDto.setId(subAreaIdContainerDto.getId());

        List <AreaDto> areaDtosAfter = areaController.getAreas(dashboardDto.getId());
        List <SubAreaDto> subAreaDtosAfter = areaController.getAllSubAreas();

        //Assert
        Assertions.assertThat(areaDtosAfter.get(0).getId()).isEqualTo(areaDtosBefore.get(0).getId());
        Assertions.assertThat(areaDtosBefore.isEmpty()).isFalse();
        Assertions.assertThat(subAreaDtosBefore.isEmpty()).isTrue();

        Assertions.assertThat(areaDtosAfter.isEmpty()).isFalse();
        Assertions.assertThat(subAreaDtosAfter.isEmpty()).isFalse();

    }


}