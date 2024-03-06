package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.repositories.*;
import no.nav.statusplattform.generated.api.DashboardDto;
import no.nav.statusplattform.generated.api.IdContainerDto;
import no.nav.statusplattform.generated.api.ServiceDto;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final DashboardController dashboardController = new DashboardController(dbContext);
    private final AreaController areaController = new AreaController(dbContext);
    private final ServiceController serviceController = new ServiceController(dbContext);
    private final OpsController opsController = new OpsController(dbContext);
    private final RecordController recordController = new RecordController(dbContext);


    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    /*@Test
    void addDashboardAreaAndService() {
        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardController.postDashboard(dashboardDto);
        IdContainerDto idContainerDto = dashboardController.postDashboard(dashboardDto);
        ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        serviceController.newService(serviceDto);
        ServiceDto retrievedServiceDto = serviceController.getService(serviceDto.getId());
        //Assert
        Assertions.assertThat(retrievedServiceDto).isNotNull();
        Assertions.assertThat(retrievedServiceDto.getId().equals(serviceDto.getId())).isTrue();
    }

    @Test
    void addAndViewServices() {
        //Arrange
        List<ServiceDto> serviceDtos = SampleDataDto.getRandomLengthListOfServiceDto();
        serviceDtos.forEach(serviceController::newService);
        ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDtoWithNameNotInList(serviceDtos);
        serviceController.newService(serviceDto);
        serviceDtos.forEach(s -> serviceController.addDependencyToService(s.getId(), serviceDto.getId()));
        //Act
        List<ServiceDto> retrievedServiceDto = serviceController.getServices();
        //Assert
        Assertions.assertThat(retrievedServiceDto.size()).isEqualTo(Stream.of(Arrays.asList(serviceDto), serviceDtos));
        Assertions.assertThat(retrievedServiceDto.containsAll(serviceDtos)).isTrue();
    }*/
}
