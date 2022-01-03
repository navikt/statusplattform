package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
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
import java.util.*;
import java.util.stream.Collectors;


class ServiceControllerTest {

    private final SampleData sampleData = new SampleData();
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final ServiceController serviceController = new ServiceController(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);
    private final RecordRepository recordRepository = new RecordRepository(dbContext);


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
        int NumberOfServices = 4;
        List<ServiceEntity> services = sampleData.getNonEmptyListOfServiceEntity(NumberOfServices);

        //Lagrer tjenester
        services.forEach(s -> s.setId(serviceRepository.save(s)));


        //Lager tilfeldig status for hver tjeneste
        Map<UUID, RecordEntity> servicesWithStatus= new HashMap<>();
        services.forEach(s -> servicesWithStatus.put(s.getId(), SampleData.getRandomizedRecordEntityForService(s)));

        //Lagrer statusen på tjenesten til DB
        servicesWithStatus.values().forEach(recordRepository::save);

        //En av tjenestene er avhengig av resten
        ServiceEntity serviceWithDependensies = services.get(0);
        List<ServiceEntity> dependencies = services.subList(1,services.size());
        UUID expectedUUIDOfServiceWithDependecies = serviceWithDependensies.getId();

        serviceRepository.addDependencyToService(serviceWithDependensies, dependencies);

        //Act
        List<ServiceDto> resultingDtos = serviceController.getServices();

        //Assert
        Assertions.assertThat(resultingDtos.size()).isEqualTo(NumberOfServices);

        //Finner alle tjenester med avhengigheter fra resultatet
        List<ServiceDto> retrievedServicesWithDependencies = resultingDtos
                .stream()
                .filter(dto -> dto.getDependencies().size() > 0)
                .collect(Collectors.toList());
        //Forventer at det bare er en tjeneste med avhengighet
        Assertions.assertThat(retrievedServicesWithDependencies.size()).isEqualTo(1);
        //Forventer at den har samme UUID som entiteten vi valgte skulle ha avhengigheter
        Assertions.assertThat(retrievedServicesWithDependencies.get(0).getId()).isEqualTo(expectedUUIDOfServiceWithDependecies);

    }

    private ServiceDto setStatus(Map<UUID, RecordEntity> servicesWithStatus, ServiceDto dto) {
        dto.setStatus(EntityDtoMappers.toStatusDto(servicesWithStatus.get(dto.getId())));
        return dto;
    }


    @Test
    void getService() {
        //Arrange
        ServiceEntity service = sampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity serviceRecord = sampleData.getRandomizedRecordEntityForService(service);
        UUID serviceRecId = recordRepository.save(serviceRecord);
        //Act
        ServiceDto serviceDto = serviceController.getService(serviceId);
        //Assert
    }

    @Test
    void newService() {
        //Arrange
        //Denne har ingen avhengigheter: TODO lage tilsvarende med avhengigheter
        ServiceEntity service = sampleData.getRandomizedServiceEntity();
        ServiceDto inputDto = EntityDtoMappers.toServiceDtoShallow(service);
        //Act
        ServiceDto actualDto  = serviceController.newService(inputDto);
        //Assert
        Assertions.assertThat(actualDto.getId()).isNotNull();
        inputDto.setId(actualDto.getId());
        Assertions.assertThat(inputDto).isEqualTo(actualDto);
    }

    @Test
    void updateService() {
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