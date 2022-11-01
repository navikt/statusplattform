package no.nav.portal.rest.api.v3.controllers;
import nav.portal.core.entities.*;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;

import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

class DashboardControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final AreaController areaController = new AreaController(dbContext);
    private final DashboardController dashboardController = new DashboardController(dbContext);
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
    void getDashboards() {
        //Arrange
        List<DashboardDto> dashboardDtos = SampleDataDto.getDashboardDtos();
        dashboardDtos.forEach(dashboardController::postDashboard);
        //Act
        List<String>dashboardNames = new ArrayList<>();
        dashboardDtos.forEach(dashboardDto -> dashboardNames.add(dashboardDto.getName()));
        List<DashboardNameIdDto> dashboardNameIdDto = dashboardController.getDashboards();
        //Assert
        List<String> retrievedNamesFromStream = dashboardNameIdDto.stream()
                .map(DashboardNameIdDto::getName)
                .collect(Collectors.toList());
        /*for(DashboardNameIdDto d: dashboardNameIdDto) {
            retrievedNames.add(d.getName());
        }*/
        //for-each loop og lambda
        //dashboardNameIdDto.forEach(d -> retrievedNames.add(d.getName()));
        Assertions.assertThat(retrievedNamesFromStream).containsExactlyInAnyOrderElementsOf(dashboardNames);
    }

    @Test
    void postDashboard() {
        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto =  areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());
        dashboardDto.setAreas(List.of(areaDto));
        //Act
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());
        DashboardDto retrievedDashboardDto = dashboardController.getDashboard(dashboardDto.getId());
        //Assert
        Assertions.assertThat(dashboardIdContainerDto.getId()).isEqualTo(dashboardDto.getId());
        Assertions.assertThat(retrievedDashboardDto.getAreas()).containsExactlyInAnyOrderElementsOf(List.of(areaDto));
    }


    @Test
    void deleteDashboard() {
        //Arrange
        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto =  areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());
        DashboardDto shouldExist = dashboardController.getDashboard(dashboardDto.getId());
        //Act
        dashboardController.deleteDashboard(dashboardDto.getId());
        //Assert
        Assertions.assertThat(shouldExist.getId()).isEqualTo(dashboardIdContainerDto.getId());
        Assertions.assertThat(dashboardController.getDashboards().isEmpty()).isTrue();
    }


    @Test
    void updateDashboard() {
        //Arrange
        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto =  areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());
        DashboardDto retrievedDashboardDtoBefore = dashboardController.getDashboard(dashboardDto.getId());
        String oldName = retrievedDashboardDtoBefore.getName();
        String newName = "Test";
        dashboardDto.setName(newName);
        DashboardUpdateDto dashboardUpdateDto = new DashboardUpdateDto().name(newName).areas(List.of(areaDto.getId()));
        //Act
        dashboardController.updateDashboard(dashboardDto.getId(), dashboardUpdateDto);
        DashboardDto retrievedDashboardDtoAfter = dashboardController.getDashboard(dashboardDto.getId());
        String retrievedName = retrievedDashboardDtoAfter.getName();
        //Assert
        Assertions.assertThat(retrievedName).isEqualTo(newName);
        Assertions.assertThat(retrievedName).isNotEqualTo(oldName);

    }

    @Test
    void getDashboard() {
        //Arrange
        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto =  areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());
        //Act
        DashboardDto retrievedDashboardDto = dashboardController.getDashboard(dashboardDto.getId());
        //Assert
        Assertions.assertThat(retrievedDashboardDto.getId()).isEqualTo(dashboardDto.getId());
    }
}