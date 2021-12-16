package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.ServiceDto;

import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


class ServiceControllerTest {

    private final SampleData sampleData = new SampleData();
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final ServiceController serviceController = new ServiceController(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);


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
        //Arrange
        List<ServiceEntity> services = sampleData.getNonEmptyListOfServiceEntity(3);
        for(ServiceEntity service : services){
            service.setId(serviceRepository.save(service));
        }
        //TODO Orlene: Legge til avhengigheter og statuser på tjenestene

        //Act
        List<ServiceDto> resultingDtos = serviceController.getServices();

        //Assert
        List<ServiceDto> expectedDtos = services.stream()
                .map(s->EntityDtoMappers.toServiceDtoDeep(s, Collections.emptyList()))
                .collect(Collectors.toList());

        Assertions.assertThat(resultingDtos).containsExactlyInAnyOrderElementsOf(expectedDtos);

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