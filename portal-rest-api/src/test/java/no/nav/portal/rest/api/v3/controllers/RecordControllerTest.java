package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.RecordDto;
import no.portal.web.generated.api.ServiceDto;

import no.portal.web.generated.api.StatusDto;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.*;

class RecordControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final ServiceController serviceController = new ServiceController(dbContext);
    private final RecordController recordController = new RecordController(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);
    private final RecordRepository recordRepository = new RecordRepository(dbContext);


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
    void addServiceStatus() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        RecordEntity record = SampleData.getRandomizedRecordEntity();
        record.setServiceId(service.getId());
        record.setId(recordRepository.save(record));
        RecordDto recordDto = EntityDtoMappers.toRecordDto(record);
        //Act
        recordController.addServiceStatus(recordDto);
        //Assert
        ServiceDto serviceDto = serviceController.getService(serviceId);
        Assertions.assertThat(serviceDto.getRecord().getStatus())
                .isEqualTo(StatusDto.fromValue(record.getStatus().getDbRepresentation()));
    }

    @Test
    void getAreas() {
        //Arrange
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);

        //Lagrer tjenester
        services.forEach(s -> s.setId(serviceRepository.save(s)));

        Map<UUID, RecordEntity> servicesWithStatus= new HashMap<>();

        //Lager tilfeldig status for hver tjeneste
        services.forEach(s -> servicesWithStatus.put(s.getId(), SampleData.getRandomizedRecordEntityForService(s)));
        //Lagrer statusen på tjenesten
        servicesWithStatus.keySet().forEach(id -> recordRepository.save(servicesWithStatus.get(id)));


        //TODO legg in avhengigheter her før mappingen:
        //serviceRepository.addDependencyToService();
        //Under bygges forventet dtoer m status og avhengigheter utifra oppsettet over:
        /*List<ServiceDto> expectedDtos = services.stream()
                .map(s-> EntityDtoMappers.toServiceDtoDeep(s, Collections.emptyList()))
                .map(dto -> setStatus(servicesWithStatus, dto))
                .collect(Collectors.toList());*/

        //TODO Orlene: Legge til avhengigheter og statuser på tjenestene
        // Først lagre avhengigheter til repository
        ServiceEntity service = SampleData.getRandomizedServiceEntityWithNameNotInList(services);
        service.setId(serviceRepository.save(service));
        UUID serviceId = service.getId();
        RecordEntity serviceRecord = SampleData.getRandomizedRecordEntityForService(service);

        // Legge til avhengighetene i mappingen, se der det står Collections.emptyList() -> Liste av avhengigheter
        serviceRepository.addDependencyToService(service, services);

        //recordRepository.save()
        recordRepository.save(serviceRecord);
        //Act
        List<RecordDto> servicesDtos = recordController.getRecordHistory(serviceId);
        //Assert
        Assertions.assertThat(servicesDtos).isNotEmpty();
    }
}