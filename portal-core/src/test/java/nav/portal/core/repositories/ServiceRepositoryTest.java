package nav.portal.core.repositories;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

class ServiceRepositoryTest {

   private SampleData sampleData = new SampleData();

   private DataSource dataSource = TestDataSource.create();

   private DbContext dbContext = new DbContext();
   private DbContextConnection connection;

   @BeforeEach
   void startConnection() {
      connection = dbContext.startConnection(dataSource);
   }

   @AfterEach
   void endConnection() {
      TestUtil.clearAllTableData(dbContext);
      connection.close();
   }

   private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

   @Test
   void save() {
      //Arrange
      ServiceEntity service = sampleData.getRandomizedServiceEntity();
      //Act
      UUID uuid = serviceRepository.save(service);
      service.setId(uuid);
      Optional<ServiceEntity> retrievedService = serviceRepository.retrieve(uuid);
      //Assert
      Assertions.assertThat(retrievedService.get()).isEqualTo(service);
   }

   @Test
   void update() {
      //Arrange
      List<ServiceEntity> services = sampleData.getNonEmptyListOfServiceEntity(2);

      UUID uuid = serviceRepository.save(services.get(0));
      services.forEach(service -> service.setId(uuid));

      ServiceEntity before = serviceRepository.retrieve(uuid).get();
      //Act
      serviceRepository.update(services.get(1));
      ServiceEntity after = serviceRepository.retrieve(uuid).get();
      //Assert
      Assertions.assertThat(before).isEqualTo(services.get(0));
      Assertions.assertThat(after).isEqualTo(services.get(1));
   }

   @Test
   void retrieve() {
      //Arrange
      ServiceEntity service = sampleData.getRandomizedServiceEntity();
      //Act
      UUID uuid = serviceRepository.save(service);
      service.setId(uuid);
      Optional<ServiceEntity> retrievedService = serviceRepository.retrieve(uuid);
      //Assert
      Assertions.assertThat(retrievedService.get()).isEqualTo(service);
   }

   @Test
   void addDependencyToService() {
      //Arrange
      ServiceEntity service1 = sampleData.getRandomizedServiceEntity();
      ServiceEntity service2 = sampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1));
      UUID uuid1 = serviceRepository.save(service1);
      UUID uuid2 = serviceRepository.save(service2);
      service1.setId(uuid1);
      service2.setId(uuid2);
      //Act
      serviceRepository.addDependencyToService(uuid1, uuid2);
      Map.Entry<ServiceEntity, List<ServiceEntity>> retrievedDependency =
              serviceRepository.retrieveOneWithDependencies(uuid1);
      //Assert
      Assertions.assertThat(retrievedDependency.getValue()).containsExactly(service2);
   }

   @Test
   void removeDependencyFromService() {
      //Arrange
      ServiceEntity service1 = sampleData.getRandomizedServiceEntity();
      ServiceEntity service2 = sampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1));
      UUID uuid1 = serviceRepository.save(service1);
      UUID uuid2 = serviceRepository.save(service2);
      service1.setId(uuid1);
      service2.setId(uuid2);
      serviceRepository.addDependencyToService(uuid1, uuid2);
      Map.Entry<ServiceEntity, List<ServiceEntity>> before =
              serviceRepository.retrieveOneWithDependencies(uuid1);
      //Act
      serviceRepository.removeDependencyFromService(uuid1, uuid2);
      Map.Entry<ServiceEntity, List<ServiceEntity>> after =
              serviceRepository.retrieveOneWithDependencies(uuid1);
      //Assert
      Assertions.assertThat(before.getValue()).containsExactly(service2);
      Assertions.assertThat(after.getValue()).isEmpty();
   }

   @Test
   void removeAllDependenciesFromService() {
      //Arrange
      List<ServiceEntity> services = sampleData.getNonEmptyListOfServiceEntity(3);
      ServiceEntity service1 = sampleData.getRandomizedServiceEntityWithNameNotInList(services);
      UUID service1Id = serviceRepository.save(service1);
      service1.setId(service1Id);

      for(ServiceEntity service : services){
         service.setId(serviceRepository.save(service));
      }
      //Act
      serviceRepository.addDependencyToService(service1, services);
      Map.Entry<ServiceEntity, List<ServiceEntity>> before =
              serviceRepository.retrieveOneWithDependencies(service1Id);
      serviceRepository.resetDependenciesOnService(service1Id);
      Map.Entry<ServiceEntity, List<ServiceEntity>> after =
              serviceRepository.retrieveOneWithDependencies(service1Id);
      //Assert
      Assertions.assertThat(before.getValue()).containsAll(services);
      Assertions.assertThat(after.getValue()).isEmpty();
   }

   @Test
   void isOtherServicesDependentOn() {
      //Arrange
      ServiceEntity service1 = sampleData.getRandomizedServiceEntity();
      ServiceEntity service2 = sampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1));
      ServiceEntity service3 = sampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1,service2));
      UUID uuid1 = serviceRepository.save(service1);
      UUID uuid2 = serviceRepository.save(service2);
      UUID uuid3 = serviceRepository.save(service3);
      //Act
      serviceRepository.addDependencyToService(uuid1, uuid2);
      boolean isDependantOnAnotherY = serviceRepository.isOtherServicesDependentOn(uuid2);
      serviceRepository.resetDependenciesOnService(uuid1);
      boolean isDependantOnAnotherN = serviceRepository.isOtherServicesDependentOn(uuid2);
      boolean isDependantOnAnotherND = serviceRepository.isOtherServicesDependentOn(uuid3);
      //Assert
      Assertions.assertThat(isDependantOnAnotherY).isTrue();
      Assertions.assertThat(isDependantOnAnotherN).isFalse();
      Assertions.assertThat(isDependantOnAnotherND).isFalse();
   }

   @Test
   void retrieveOneWithDependencies() {
      //Arrange
      List<ServiceEntity> services = sampleData.getNonEmptyListOfServiceEntity(3);
      ServiceEntity service1 = sampleData.getRandomizedServiceEntityWithNameNotInList(services);
      UUID service1Id = serviceRepository.save(service1);
      service1.setId(service1Id);

      for(ServiceEntity service : services){
         service.setId(serviceRepository.save(service));
      }

      serviceRepository.addDependencyToService(service1, services);
      //Act
      Map.Entry<ServiceEntity, List<ServiceEntity>> retrievedService =
              serviceRepository.retrieveOneWithDependencies(service1Id);
      //Assert
      Assertions.assertThat(retrievedService.getValue()).containsExactlyElementsOf(services);
      Assertions.assertThat(retrievedService.getKey()).isEqualTo(service1);
   }

   @Test
   void retriveAll() {
      //Arrange
      List<ServiceEntity> services = sampleData.getNonEmptyListOfServiceEntity(3);
      for(ServiceEntity service : services){
         service.setId(serviceRepository.save(service));
      }
      //TODO legge til dependencies/ gjøre assertioens motsatt vei: Det vi kontrollerer .someComparingFunction(Forventet verdi)
      //Act
      Map<ServiceEntity, List<ServiceEntity>> allRetrieved =
              serviceRepository.retrieveAll();
      //Assert
      Assertions.assertThat(allRetrieved.size()).isEqualTo(services.size());
      Assertions.assertThat(allRetrieved.keySet()).containsAll(services);
   }

   @Test
   void doesEntryExist() {
      //Arrange
      ServiceEntity service1 = sampleData.getRandomizedServiceEntity();
      ServiceEntity service2 = sampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1));
      UUID uuid1 = serviceRepository.save(service1);
      UUID uuid2 = serviceRepository.save(service2);
      service1.setId(uuid1);
      service2.setId(uuid2);
      //Act
      Optional<ServiceEntity> exists = serviceRepository.retrieve(uuid1);
      boolean shouldExist =  serviceRepository.doesEntryExist(uuid1);
      serviceRepository.delete(uuid2);
      boolean shouldNotExist =  serviceRepository.doesEntryExist(uuid2);
      //Assert
      Assertions.assertThat(shouldExist).isTrue();
      Assertions.assertThat(shouldNotExist).isFalse();

   }

   @Test
   void delete() {
      //Arrange
      ServiceEntity service = sampleData.getRandomizedServiceEntity();
      //Act
      UUID uuid = serviceRepository.save(service);
      service.setId(uuid);
      int deleted = serviceRepository.delete(uuid);
      Optional<ServiceEntity> shouldBeEmpty = serviceRepository.retrieve(uuid);
      //Assert
      Assertions.assertThat(deleted).isEqualTo(1);
      Assertions.assertThat(shouldBeEmpty).isEmpty();
   }


   //TODO: Skriv flere tester som denne: Orlene og Bjørg
   @Test
   void save_and_retrieve_service() {
      // Arrange
      ServiceEntity service = sampleData.getRandomizedServiceEntity();

      // Act
      UUID uuid = serviceRepository.save(service);
      Optional<ServiceEntity> retrievedService = serviceRepository.retrieve(uuid);
      service.setId(uuid);


      // Assert
      Assertions.assertThat(retrievedService.orElseGet(() -> fail("klarte ikke legge til i db")))
              .isEqualTo(service);
   }

   //Skrive test for å legge til avhengigheter:

   @Test
   void save_and_confirm_dependencies() {
      ServiceEntity service1 = new ServiceEntity()
              .setName("Hei")
              .setType(ServiceType.TJENESTE)
              .setTeam("Status")
              .setMonitorlink("Yes")
              .setDescription("Cookie data")
              .setLogglink("No");

      ServiceEntity service2 = new ServiceEntity()
              .setName("Bye")
              .setType(ServiceType.TJENESTE)
              .setTeam("Status")
              .setMonitorlink("Yes")
              .setDescription("Cookie data")
              .setLogglink("No");
      // Act
      UUID serviceId1 = serviceRepository.save(service1);
      service1.setId(serviceId1);
      UUID serviceId2 = serviceRepository.save(service2);
      service2.setId(serviceId2);
      serviceRepository.addDependencyToService(serviceId1, serviceId2);
      Map.Entry<ServiceEntity, List<ServiceEntity>> retrievedDependency =
              serviceRepository.retrieveOneWithDependencies(serviceId1);
      // Assert
      Assertions.assertThat(retrievedDependency.getValue()).containsExactly(service2);
   }

   @Test
   void save_and_retrieve_service_2() {
      // Arrange
      /*private String name;
    private ServiceType type;
    private String team;
    private String monitorlink;
    private String description;
    private String logglink;*/


      ServiceEntity service = new ServiceEntity()
              .setName("Hei")
              .setType(ServiceType.TJENESTE)
              .setTeam("Digital")
              .setMonitorlink("Yes")
              .setDescription("Orlenes data")
              .setLogglink("No");
      // Act
      UUID uuid = serviceRepository.save(service);
      service.setId(uuid);
      Optional<ServiceEntity> retrievedService = serviceRepository.retrieve(uuid);


      // Assert
      Assertions.assertThat(retrievedService.orElseGet(() -> fail("klarte ikke legge til i db")))
              .isEqualTo(service);// Assert

   }


}