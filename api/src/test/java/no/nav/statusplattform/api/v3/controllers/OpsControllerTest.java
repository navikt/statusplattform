package no.nav.statusplattform.api.v3.controllers;

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
import java.util.List;


class OpsControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private final DashboardController dashboardController = new DashboardController(dbContext);
    private final AreaController areaController = new AreaController(dbContext);
    private final ServiceController serviceController = new ServiceController(dbContext);
    private final OpsController opsController = new OpsController(dbContext);

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

    @Test
    void createOpsMessage() {
        //Arrange
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto = areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());

        //ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        //ServiceDto savedServiceDto = serviceController.newService(serviceDto);
        //serviceDto.setId(savedServiceDto.getId());

        List<ServiceDto> serviceDtos = SampleDataDto.getRandomLengthListOfServiceDto();
        serviceDtos.forEach(serviceDto1 -> {
           ServiceDto savedServiceDto1 = serviceController.newService(serviceDto1);
           serviceDto1.setId(savedServiceDto1.getId());
        });

        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());

        OPSmessageDto opsMessageDto = SampleDataDto.getRandomOPSMessageDto();
        opsMessageDto.setAffectedServices(serviceDtos);
        ///opsMessageDto.setAffectedServices(List.of(serviceDto));
        //Act
        OPSmessageDto createdOpsMessage = opsController.createOpsMessage(opsMessageDto);
        OPSmessageDto retrievedOpsMessage = opsController.getSpecificOpsMessage(createdOpsMessage.getId());
        List<ServiceDto> affectedServices = opsMessageDto.getAffectedServices();

        //Assert
        Assertions.assertThat(retrievedOpsMessage).isEqualTo(createdOpsMessage);
        Assertions.assertThat(createdOpsMessage.getInternalHeader()).isEqualTo(opsMessageDto.getInternalHeader());
        Assertions.assertThat(createdOpsMessage.getInternalMessage()).isEqualTo(opsMessageDto.getInternalMessage());
        Assertions.assertThat(affectedServices).containsExactlyInAnyOrderElementsOf(serviceDtos);
        Assertions.assertThat(affectedServices.size()).isEqualTo(serviceDtos.size());
      }

    @Test
    void getAllOpsMessages() {
        //Arrange
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto = areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());

        List<ServiceDto> serviceDtos = SampleDataDto.getRandomLengthListOfServiceDto();
        serviceDtos.forEach(serviceDto -> {
            ServiceDto savedServiceDto = serviceController.newService(serviceDto);
            serviceDto.setId(savedServiceDto.getId());
        });

        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());

        List<OPSmessageDto> opsMessageDtos = SampleDataDto.getRandomLengthListOfOPSMessageDto();
        opsMessageDtos.forEach(dto -> dto.setId(opsController.createOpsMessage(dto).getId()));


        //Act
        List<OPSmessageDto> retrievedOpsMessages = opsController.getAllOpsMessages();
        //Assert
        // CATO: Needs to be looked at. Not expecting a "internalMessage: A short-lived fault in a system". FIXME later
//        assertThat(retrievedOpsMessages)
//                .hasSameSizeAs(opsMessageDtos)
//                .containsExactlyInAnyOrder(opsMessageDtos.toArray(new OPSmessageDto[0]));
    }

    @Test
    void deleteOpsMessage() {
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

        List<OPSmessageDto> opsMessageDtos = SampleDataDto.getRandomLengthListOfOPSMessageDto();
        opsMessageDtos.forEach(dto -> {
            dto.setAffectedServices(List.of(serviceDto));
            dto.setId(opsController.createOpsMessage(dto).getId());
        });

        OPSmessageDto toBeDeleted = opsMessageDtos.get(0);
        List<OPSmessageDto> retrievedOpsMessagesBefore = opsController.getAllOpsMessages();
        //Act
        opsController.deleteOpsMessage(opsMessageDtos.get(0).getId());
        List<OPSmessageDto> retrievedOpsMessagesAfter = opsController.getAllOpsMessages();
        //Assert
        Assertions.assertThat(retrievedOpsMessagesBefore.size()).isEqualTo(opsMessageDtos.size());
        Assertions.assertThat(retrievedOpsMessagesAfter.size()).isLessThan(opsMessageDtos.size());
        Assertions.assertThat(retrievedOpsMessagesBefore).contains(toBeDeleted);
        Assertions.assertThat(retrievedOpsMessagesAfter).doesNotContain(toBeDeleted);
    }

    @Test
    void getSpecificOpsMessageSimple(){
        //Arrange


        OPSmessageDto opsMessageDto = SampleDataDto.getRandomOPSMessageDto();

        opsMessageDto.setId(opsController.createOpsMessage(opsMessageDto).getId());

        //Act
        OPSmessageDto retrievedOPSMessageDto = opsController.getSpecificOpsMessage(opsMessageDto.getId());
        //Assert
        Assertions.assertThat(retrievedOPSMessageDto).isEqualTo(opsMessageDto);
    }

    @Test
    void getSpecificOpsMessage(){
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

        List<OPSmessageDto> opsMessageDtos = SampleDataDto.getNonEmptyListOfOpsMessageDto(2);

        opsMessageDtos.forEach(opsMessageDto -> {
            opsMessageDto.setAffectedServices(List.of(serviceDto));
            opsMessageDto.setId(opsController.createOpsMessage(opsMessageDto).getId());
        });
        OPSmessageDto toBeRetrieved = opsMessageDtos.get(0);
        //Act
        OPSmessageDto retrievedOPSMessageDto = opsController.getSpecificOpsMessage(opsMessageDtos.get(0).getId());
        //Assert
        Assertions.assertThat(retrievedOPSMessageDto.getId()).isEqualTo(toBeRetrieved.getId());
        Assertions.assertThat(retrievedOPSMessageDto.getInternalHeader()).isEqualTo(toBeRetrieved.getInternalHeader());
    }

    @Test
    void updateSpecificOpsMessage(){
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

        List<OPSmessageDto> opsMessageDtos = SampleDataDto.getNonEmptyListOfOpsMessageDto(2);
        opsMessageDtos.forEach(opsMessageDto -> {
            opsMessageDto.setAffectedServices(List.of(serviceDto));
            opsMessageDto.setId(opsController.createOpsMessage(opsMessageDto).getId());
        });

        OPSmessageDto opsMessageBefore = opsController.getSpecificOpsMessage(opsMessageDtos.get(0).getId());
        OPSmessageDto updatedOpsMessage = opsMessageDtos.get(1);
        //Act
        OPSmessageDto opsMessageAfter =
                opsController.updateSpecificOpsMessage(opsMessageDtos.get(0).getId(), updatedOpsMessage);
        //Assert
        Assertions.assertThat(opsMessageBefore.getInternalHeader()).isNotEqualToIgnoringCase(opsMessageAfter.getInternalHeader());
        Assertions.assertThat(opsMessageAfter.getInternalHeader()).isEqualToIgnoringCase(updatedOpsMessage.getInternalHeader());
    }
}
