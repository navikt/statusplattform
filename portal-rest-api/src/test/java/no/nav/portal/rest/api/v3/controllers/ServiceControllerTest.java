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
        List<ServiceEntity> services = sampleData.getNonEmptyListOfServiceEntity(4);

        //Lagrer tjenester
        services.forEach(s -> s.setId(serviceRepository.save(s)));

        Map<UUID, RecordEntity> servicesWithStatus= new HashMap<>();

        //Lager tilfeldig status for hver tjeneste
        services.forEach(s -> servicesWithStatus.put(s.getId(), SampleData.getRandomizedRecordEntityForService(s)));
        //Lagrer statusen på tjenesten
        servicesWithStatus.keySet().forEach(id -> recordRepository.save(servicesWithStatus.get(id)));

        //En av tjenestene er avhengig av resten
        ServiceEntity serviceWithDependensies = services.get(0);
        List<ServiceEntity> dependencies = services.subList(1,services.size());

        serviceRepository.addDependencyToService(serviceWithDependensies, dependencies);

        //Under bygges forventet dtoer m status og avhengigheter utifra oppsettet over:
        List<ServiceDto> expectedDtos = services.stream()
                .map(s->
                        EntityDtoMappers.toServiceDtoDeep(s,
                                s.getId().equals(serviceWithDependensies.getId()) ? dependencies :
                                Collections.emptyList()))
                .map(dto -> setStatus(servicesWithStatus, dto))
                .collect(Collectors.toList());


        //TODO Orlene: Legge til avhengigheter og statuser på tjenestene
        // Først lagre avhengigheter til repository


        // Legge til avhengighetene i mappingen, se der det står Collections.emptyList() -> Liste av avhengigheter

        //recordRepository.save()
       // UUID serviceRecId = recordRepository.save(serviceRecord);
        //Act
        List<ServiceDto> resultingDtos = serviceController.getServices();

        //Assert
        Assertions.assertThat(resultingDtos).containsExactlyInAnyOrderElementsOf(expectedDtos);


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