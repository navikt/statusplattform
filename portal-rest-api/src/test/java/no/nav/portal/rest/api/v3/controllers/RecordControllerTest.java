package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;

import org.actioncontroller.PathParam;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

class RecordControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final DashboardController dashboardController = new DashboardController(dbContext);
    private final AreaController areaController = new AreaController(dbContext);
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
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto = areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());

        ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        ServiceDto savedServiceDto = serviceController.newService(serviceDto);
        serviceDto.setId(savedServiceDto.getId());

        RecordDto recordDto = SampleDataDto.getRandomizedRecordDto();
        recordDto.setId(recordDto.getId());
        recordDto.setServiceId(serviceDto.getId());

        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());

        areaController.addServiceToArea(areaDto.getId(), serviceDto.getId());
        List<RecordDto> recordStatusOnServiceBefore = recordController.getRecordHistory(serviceDto.getId());

        //Act
        recordController.addServiceStatus(recordDto);
        dashboardController.getDashboard(dashboardDto.getId());
        //Assert
        List<RecordDto> recordStatusOnServiceAfter = recordController.getRecordHistory(serviceDto.getId());

        Assertions.assertThat(recordStatusOnServiceBefore).isEmpty();
        Assertions.assertThat(recordStatusOnServiceAfter).isNotEmpty();
        Assertions.assertThat(recordStatusOnServiceAfter.get(0).getServiceId())
               .isEqualTo(serviceDto.getId());
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

    @Test
    void getRecordHistory() {
        //Arrange
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        serviceEntity.setId(serviceRepository.save(serviceEntity));
        UUID serviceID = serviceEntity.getId();
        List<RecordEntity> records = new ArrayList<>();
        records.add(SampleData.getRandomizedRecordEntityForService(serviceEntity));
        records.add(SampleData.getRandomizedRecordEntityForService(serviceEntity));
        records.add(SampleData.getRandomizedRecordEntityForService(serviceEntity));
        records.add(SampleData.getRandomizedRecordEntityForService(serviceEntity));
        records.add(SampleData.getRandomizedRecordEntityForService(serviceEntity));
        int i = 2;
        for(RecordEntity record : records){
            record.setCreated_at(ZonedDateTime.now().minusDays(i).truncatedTo(ChronoUnit.SECONDS));
            record.setId(RecordRepository.saveRecordBackInTime(record,dbContext));
            i++;
        }
        //Act
        List<RecordDto> retrievedRecordDtos = recordController.getRecordHistory(serviceID);
        //Assert
        List<RecordEntity> retrievedRecords = retrievedRecordDtos
                .stream().map(EntityDtoMappers::toRecordEntity).collect(Collectors.toList());
        //Assert
        Assertions.assertThat(retrievedRecordDtos.size()).isEqualTo(records.size());
        Assertions.assertThat(retrievedRecords).containsAll(records);
    }

    private RecordDto truncateCreatedAtToSeconds(RecordDto dto){
        return  dto.timestamp(dto.getTimestamp().truncatedTo(ChronoUnit.SECONDS));
    }

}