package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.portal.web.generated.api.RecordDto;
import no.portal.web.generated.api.ServiceDto;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    void addAndViewServices() {
        //Arrange
        List<ServiceDto> servicesDto = SampleDataDto.getRandomLengthListOfServiceDto();

        //Lagrer tjenester
        servicesDto.forEach(serviceController::newService);

        //Act
        List<ServiceDto> retrievedServiceDto = serviceController.getServices();
        //Assert
        Assertions.assertThat(retrievedServiceDto.size()).isEqualTo(servicesDto.size());
        Assertions.assertThat(retrievedServiceDto.containsAll(servicesDto)).isTrue();
    }*/

      @Test
    void addAndViewService() {
        ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        serviceController.newService(serviceDto);
        ServiceDto retrievedServiceDto = serviceController.getService(serviceDto.getId());
        //Assert
        Assertions.assertThat(retrievedServiceDto).isNotNull();

    }
}
