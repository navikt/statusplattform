package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.infrastructure.PortalRestPrincipal;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.ServiceRepositoryHelper;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.ServiceStatusDto;
import no.portal.web.generated.api.ServiceTypeDto;
import no.portal.web.generated.api.StatusDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


class ServiceControllerTest {
    private SampleData sampleData = new SampleData();

    private DataSource dataSource = TestDataSource.create();

    private DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    private final ServiceController serviceController;
    private final ServiceRepository serviceRepository;


    public ServiceControllerTest(DbContext dbContext) {

        this.serviceController = new ServiceController(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
    }

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
    }

    @AfterEach
    void endConnection() {
        TestUtil.clearAllTableData(dbContext);
        connection.close();
    }

    @Test
    void getServices() {
        //Assign
        List<ServiceEntity> services = sampleData.getNonEmptyListOfServiceEntity(3);
        for(ServiceEntity service : services){
            service.setId(serviceRepository.save(service));
        }
        //Arrange
        List<ServiceDto> servicesDto = serviceController.getServices();

        //Act

        //Assert


    }

    @Test
    void getService() {
    }

    @Test
    void newService() {
    }

    @Test
    void updateService() {
    }

    @Test
    void testNewService() {
    }

    @Test
    void removeDependencyFromService() {
    }

    @Test
    void deleteService() {
    }

    @Test
    void getServicetypes() {
    }

    @Test
    void getServiceStatuses() {
    }
}