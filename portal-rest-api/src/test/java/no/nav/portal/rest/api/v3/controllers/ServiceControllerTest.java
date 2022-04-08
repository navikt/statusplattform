package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import nav.portal.core.util.MockDataGenerator;
import nav.portal.jobs.recordAggregation.RecordCompressor;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.ServiceDto;

import no.portal.web.generated.api.ServiceHistoryDto;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;


class ServiceControllerTest {

    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final ServiceController serviceController = new ServiceController(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);
    private final RecordRepository recordRepository = new RecordRepository(dbContext);
    private final RecordCompressor recordCompressor = new RecordCompressor(dbContext);

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        recordCompressor.setDataSource(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    @Test
    void getServices() {
        //Arrange
        int NumberOfServices = 4;
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(NumberOfServices);

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
                .filter(dto -> dto.getComponentDependencies().size() +dto.getServiceDependencies().size() > 0)
                .collect(Collectors.toList());
        //Forventer at det bare er en tjeneste med avhengighet
        Assertions.assertThat(retrievedServicesWithDependencies.size()).isEqualTo(1);
        //Forventer at den har samme UUID som entiteten vi valgte skulle ha avhengigheter
        Assertions.assertThat(retrievedServicesWithDependencies.get(0).getId()).isEqualTo(expectedUUIDOfServiceWithDependecies);

    }

    /*private ServiceDto setStatus(Map<UUID, RecordEntity> servicesWithStatus, ServiceDto dto) {
        dto.setStatus(EntityDtoMappers.toStatusDto(servicesWithStatus.get(dto.getId())));
        return dto;
    }*/


    @Test
    void getService() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        //Act
        ServiceDto serviceDto = serviceController.getService(serviceId);
        //Assert
        Assertions.assertThat(serviceDto).isNotNull();
        Assertions.assertThat(serviceDto.getId()).isNotNull();
    }

    @Test
    void newService() {
        //Arrange
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        //Act
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        //Assert
        Assertions.assertThat(serviceDto).isNotNull();
        Assertions.assertThat(serviceDto.getId()).isNotNull();
    }

    @Test
    void updateService() {
        //Arrange
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(2);
        ServiceDto beforeDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(services.get(0)));
        UUID beforeId = beforeDto.getId();
        ServiceDto afterDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(services.get(1)));
        //Act
        serviceController.updateService(beforeId, afterDto);
        ServiceDto updated = serviceController.getService(beforeId);
        //Assert
        Assertions.assertThat(updated).isNotEqualTo(beforeDto);
        Assertions.assertThat(updated).isEqualTo(afterDto);
    }

    @Test
    void removeDependencyFromService() {
        //Arrange
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
        ServiceEntity service = SampleData.getRandomizedServiceEntityWithNameNotInList(services);
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);

        /*for(ServiceEntity serv : services){
            serv.setId(serviceRepository.save(serv));
        }*/

        services.forEach(service1 -> service1.setId(serviceRepository.save(service1)));

        UUID dependency = services.get(0).getId();
        ServiceEntity removed = services.get(0);
        serviceRepository.addDependencyToService(service, services);
        Map.Entry<ServiceEntity, List<ServiceEntity>> before =
                serviceRepository.retrieveOneWithDependencies(serviceId);
        //Act
        serviceController.removeDependencyFromService(serviceId,dependency);
        Map.Entry<ServiceEntity, List<ServiceEntity>> after =
                serviceRepository.retrieveOneWithDependencies(serviceId);

        //Assert
        Assertions.assertThat(before.getValue().size()).isEqualTo(services.size());
        Assertions.assertThat(before.getValue()).contains(removed);
        Assertions.assertThat(after.getValue()).doesNotContain(removed);
    }

    @Test
    void deleteService() {
        //Arrange
        int NumberOfServices = 4;
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(NumberOfServices);

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
        UUID UUIDServiceWithDependecies = serviceWithDependensies.getId();

        serviceRepository.addDependencyToService(serviceWithDependensies, dependencies);
        Boolean exists = serviceRepository.doesEntryExist(UUIDServiceWithDependecies);
        //Act
        serviceController.deleteService(UUIDServiceWithDependecies);
        //Assert
        Assertions.assertThat(exists).isTrue();
        Assertions.assertThat(serviceRepository.doesEntryExist(UUIDServiceWithDependecies)).isTrue();
        Assertions.assertThat(serviceRepository.retrieve(UUIDServiceWithDependecies)).isNotEmpty();
        Assertions.assertThat(serviceRepository.retrieve(UUIDServiceWithDependecies).orElse(null).getDeleted());
        //Assertions.assertThat(serviceRepository.retrieve(UUIDServiceWithDependecies).get().getDeleted()).isTrue();
        //TODO: ORLENE/BJØRG:
        // Lag 4 forskjellige tester her:
        //                                - En som har avhengighet til annen tjeneste/(er)
        //                                - En som har avhengighet til komponent/(er)
        //                                - En som som ligger i et område
        //                                - En som ikke har noen avhengigheter eller områder
        //  Videre: prøv å identifisere lignende tilfeller
        //  Dersom vi har tester av disse typene blir det mye lettere å feilsøke når noe ikke virker

    }

    @Test
    void getServiceHistoryTwelveMonthsBack() {
        //Arrange
        List<ServiceEntity> serviceEntities = SampleData.getNonEmptyListOfServiceEntity(1);
        serviceEntities.forEach(s -> s.setId(serviceRepository.save(s)));

        Map<Month, String> monthMapBetweenLanguages = createMapBetweenEngAndNor();
        String dateNowInNorwegian = monthMapBetweenLanguages.get(LocalDate.now().getMonth());

        int numberOfDays = 365;
        int minBetweenUpdates = 60;

        UUID serviceID = serviceEntities.get(0).getId();
        Map<UUID, Map<Integer, List<RecordEntity>>> generatedData = MockDataGenerator.generateRandomStatusesForAllServices(serviceEntities, numberOfDays, minBetweenUpdates);
        MockDataGenerator.saveRecordsToTableForAllServices(generatedData, dbContext);
        recordCompressor.run();

        //Act
        ServiceHistoryDto result = serviceController.getServiceHistoryTwelveMonthsBack(serviceID);

        //Assert
        Assertions.assertThat(result.getHistory().get(0).getMonth())
                .isEqualTo(dateNowInNorwegian);
        Assertions.assertThat(result.getHistory().size()).isEqualTo(12);
    }



//  HELPERS below
    private Map<Month, String> createMapBetweenEngAndNor() {
        Map<Month, String> monthMapBetweenLanguages = new HashMap();
        monthMapBetweenLanguages.put(Month.JANUARY, "Januar");
        monthMapBetweenLanguages.put(Month.FEBRUARY, "Februar");
        monthMapBetweenLanguages.put(Month.MARCH, "Mars");
        monthMapBetweenLanguages.put(Month.APRIL, "April");
        monthMapBetweenLanguages.put(Month.MAY, "Mai");
        monthMapBetweenLanguages.put(Month.JUNE, "Juni");
        monthMapBetweenLanguages.put(Month.JULY, "Juli");
        monthMapBetweenLanguages.put(Month.AUGUST, "August");
        monthMapBetweenLanguages.put(Month.SEPTEMBER, "September");
        monthMapBetweenLanguages.put(Month.OCTOBER, "Oktober");
        monthMapBetweenLanguages.put(Month.NOVEMBER, "November");
        monthMapBetweenLanguages.put(Month.DECEMBER, "Desember");

        return monthMapBetweenLanguages;
    }
}