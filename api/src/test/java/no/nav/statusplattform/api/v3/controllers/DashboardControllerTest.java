package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.openingHours.OpeningHoursDailyMap;
import nav.statusplattform.core.repositories.OpeningHoursRepository;
import nav.statusplattform.core.repositories.ServiceRepository;
import nav.statusplattform.core.repositories.TestDataSource;
import nav.statusplattform.core.repositories.TestUtil;
import no.nav.statusplattform.generated.api.*;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class DashboardControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final AreaController areaController = new AreaController(dbContext);
    private final DashboardController dashboardController = new DashboardController(dbContext);
    private final ServiceController serviceController = new ServiceController(dbContext);
    private final OpsController opsController = new OpsController(dbContext);
    private final OpeningHoursController openingHoursController = new OpeningHoursController(dbContext);

    private final OpeningHoursRepository openingHoursRepository = new OpeningHoursRepository(dbContext);
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
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto =  areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());


        List<ServiceDto> serviceDtos = SampleDataDto.getRandomLengthListOfServiceDto();
        serviceDtos.forEach(serviceDto -> {
            ServiceDto savedServiceDto = serviceController.newService(serviceDto);
            serviceDto.setId(savedServiceDto.getId());
        });
        serviceDtos.forEach(serviceDto -> areaController.addServiceToArea(areaDto.getId(), serviceDto.getId()));

        List<ServiceDto> affectedServices = List.of(serviceDtos.get(0));
        OPSmessageDto opsMessageDto = SampleDataDto.getRandomOPSMessageDto();
        opsMessageDto.setAffectedServices(affectedServices);
        opsMessageDto.setId(opsController.createOpsMessage(opsMessageDto).getId());


        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());
        //Act
        DashboardDto retrievedDashboardDto = dashboardController.getDashboard(dashboardDto.getId());
        //Assert
        Assertions.assertThat(retrievedDashboardDto.getId()).isEqualTo(dashboardDto.getId());
        Assertions.assertThat(retrievedDashboardDto.getOpsMessages().get(0).getAffectedServices()).isEqualTo(affectedServices);
    }

    @Test
    void getDashboardOpeningHours() {
        //Arrange
        OHRuleDto oHRuleDto = SampleDataDto.getOHRuleDtoSimple();
        OHRuleDto savedOHRuleDto = openingHoursController.newRule(oHRuleDto);
        oHRuleDto.setId(oHRuleDto.getId());
        savedOHRuleDto.setId(savedOHRuleDto.getId());

        OHGroupThinDto oHGroupThinDto = new OHGroupThinDto()
                .name("basic")
                .rules(List.of(savedOHRuleDto.getId()));
        openingHoursController.newGroup(oHGroupThinDto);

        ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        ServiceDto savedServiceDto = serviceController.newService(serviceDto);
        savedServiceDto.setId(savedServiceDto.getId());

        openingHoursController.setOpeningHoursToService(oHGroupThinDto.getId(), savedServiceDto.getId());

        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto =  areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());

        areaController.addServiceToArea(areaDto.getId(), savedServiceDto.getId());


        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());


        OpeningHoursDailyMap.populateMap(openingHoursRepository);
        //Act
        DashboardDto retrievedDashboardDto = dashboardController.getDashboard(dashboardDto.getId());
        List<AreaDto>retrievedAreasList = retrievedDashboardDto.getAreas();
        AreaDto retrievedArea = retrievedAreasList.get(0);
        List<ServiceDto>retrievedServicesList = retrievedArea.getServices();
        ServiceDto retrievedService = retrievedServicesList.get(0);
        OHdisplayDto retrievedOHDisplayDto = retrievedService.getOhDisplay();

        //Assert
        Assertions.assertThat(retrievedDashboardDto.getId()).isEqualTo(dashboardDto.getId());
        Assertions.assertThat(retrievedOHDisplayDto.getRule()).isEqualTo(oHRuleDto.getRule());
        Assertions.assertThat(retrievedOHDisplayDto.getName()).isEqualTo(oHRuleDto.getName());


    }
}
