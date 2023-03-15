package nav.portal.core.repositories;

import nav.portal.core.entities.MaintenanceEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.*;


import static org.assertj.core.api.Assertions.fail;

class ServiceRepositoryTest {

   private final DataSource dataSource = TestDataSource.create();

   private final DbContext dbContext = new DbContext();
   private DbContextConnection connection;

   @BeforeEach
   void startConnection() {
      connection = dbContext.startConnection(dataSource);
      TestUtil.clearAllTableData(dbContext);
   }

   @AfterEach
   void endConnection() {
      connection.close();
   }

   private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);


   @Test
   void saveMaintenance() {
      //Arrange
      ServiceEntity service = SampleData.getRandomizedServiceEntity();
      UUID serviceId = serviceRepository.save(service);
      service.setId(serviceId);
      MaintenanceEntity maintenance = SampleData.getRandomizedMaintenanceEntity();
      maintenance.setServiceId(serviceId);
      //Act
      UUID maintenanceId = serviceRepository.saveMaintenance(maintenance);
      maintenance.setId(maintenanceId);
      List<MaintenanceEntity> retrievedMaintenance = serviceRepository.getMaintenanceForService(serviceId);
      //Assert
      Assertions.assertThat(retrievedMaintenance.size()).isEqualTo(1);
      Assertions.assertThat(retrievedMaintenance.get(0).getServiceId().equals(service.getId())).isTrue();
   }

   @Test
   void getMaintenanceForService() {
      //Arrange
      ServiceEntity service = SampleData.getRandomizedServiceEntity();
      UUID serviceId = serviceRepository.save(service);
      service.setId(serviceId);

      MaintenanceEntity maintenance1 = SampleData.getRandomizedMaintenanceEntity();
      maintenance1.setServiceId(serviceId);
      MaintenanceEntity maintenance2 = SampleData.getRandomizedMaintenanceEntity();
      maintenance2.setServiceId(serviceId);

      UUID maintenance1Id = serviceRepository.saveMaintenance(maintenance1);
      maintenance1.setId(maintenance1Id);
      UUID maintenance2Id = serviceRepository.saveMaintenance(maintenance2);
      maintenance2.setId(maintenance2Id);
      //Act
      List<MaintenanceEntity> retrievedMaintenance = serviceRepository.getMaintenanceForService(serviceId);
      //Assert
      Assertions.assertThat(retrievedMaintenance.size()).isEqualTo(2);
      Assertions.assertThat(retrievedMaintenance.get(0).getServiceId().equals(service.getId())).isTrue();
      Assertions.assertThat(retrievedMaintenance.get(1).getServiceId().equals(service.getId())).isTrue();
      Assertions.assertThat(retrievedMaintenance.get(0).getServiceId()).isEqualTo(maintenance1.getServiceId());
      Assertions.assertThat(retrievedMaintenance.get(0).getId()).isEqualTo(maintenance1.getId());
      Assertions.assertThat(retrievedMaintenance.get(1).getServiceId()).isEqualTo(maintenance2.getServiceId());
      Assertions.assertThat(retrievedMaintenance.get(1).getId()).isEqualTo(maintenance2.getId());
   }

   @Test
   void save() {
      //Arrange
      ServiceEntity service = SampleData.getRandomizedServiceEntity();
      //Act
      UUID uuid = serviceRepository.save(service);
      service.setId(uuid);
      Optional<ServiceEntity> retrievedService = serviceRepository.retrieve(uuid);
      //Assert
      retrievedService.ifPresent(serviceEntity -> Assertions.assertThat(serviceEntity).isEqualTo(service));
   }


   @Test
   void update() {
      //Arrange
      List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(2);

      UUID uuid = serviceRepository.save(services.get(0));
      services.forEach(service -> service.setId(uuid));
      ServiceEntity before = serviceRepository.retrieve(uuid).orElse(null);
      //Act
      serviceRepository.update(services.get(1));
      ServiceEntity after = serviceRepository.retrieve(uuid).orElse(null);
      //Assert
      Assertions.assertThat(before).isEqualTo(services.get(0));
      Assertions.assertThat(after).isEqualTo(services.get(1));
   }

   @Test
   void retrieve() {
      //Arrange
      ServiceEntity service = SampleData.getRandomizedServiceEntity();
      //Act
      UUID uuid = serviceRepository.save(service);
      service.setId(uuid);
      Optional<ServiceEntity> retrievedService = serviceRepository.retrieve(uuid);
      //Assert
      retrievedService.ifPresent(serviceEntity -> Assertions.assertThat(serviceEntity).isEqualTo(service));
   }

   @Test
   void addDependencyToService() {
      //Arrange
      ServiceEntity service1 = SampleData.getRandomizedServiceEntity();
      ServiceEntity service2 = SampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1));
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
      ServiceEntity service1 = SampleData.getRandomizedServiceEntity();
      ServiceEntity service2 = SampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1));
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
   void resetDependenciesOnService() {
      //Arrange
      List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntityWithUid(3);
      ServiceEntity selectedService = services.get(0);
      services = services.subList(1,3);
      selectedService.setId(serviceRepository.save(selectedService));
      UUID selectedServiceId = selectedService.getId();

      services.forEach(service -> service.setId(serviceRepository.save(service)));
      UUID dependantServiceId = services.get(0).getId();
      UUID ServiceChosenServiceIsDependentOnId = services.get(1).getId();
      serviceRepository.addDependencyToService(ServiceChosenServiceIsDependentOnId, selectedServiceId);//Service chosenService is dependent on
      serviceRepository.addDependencyToService(selectedServiceId, dependantServiceId);//Chosen service and its dependency

      Map.Entry<ServiceEntity, List<ServiceEntity>> selectedServiceRetrievedDependenciesBefore =
              serviceRepository.retrieveOneWithDependencies(selectedServiceId);

      Map.Entry<ServiceEntity, List<ServiceEntity>> serviceChosenServiceIsDependentOnRetrievedDependenciesBefore =
              serviceRepository.retrieveOneWithDependencies(ServiceChosenServiceIsDependentOnId);
      //Act
      serviceRepository.resetDependenciesOnService(selectedServiceId);
      Map.Entry<ServiceEntity, List<ServiceEntity>> selectedServiceRetrievedDependenciesAfter =
              serviceRepository.retrieveOneWithDependencies(selectedServiceId);

      Map.Entry<ServiceEntity, List<ServiceEntity>> serviceChosenServiceIsDependentOnRetrievedDependenciesAfter =
              serviceRepository.retrieveOneWithDependencies(ServiceChosenServiceIsDependentOnId);
      //Assert
      Assertions.assertThat(selectedServiceRetrievedDependenciesBefore.getKey().getId().equals(selectedServiceId)).isTrue();
      Assertions.assertThat(selectedServiceRetrievedDependenciesBefore.getValue().isEmpty()).isFalse();
      Assertions.assertThat(selectedServiceRetrievedDependenciesBefore.getValue().contains(services.get(0))).isTrue();
      Assertions.assertThat(selectedServiceRetrievedDependenciesAfter.getKey().getId().equals(selectedServiceId)).isTrue();
      Assertions.assertThat(selectedServiceRetrievedDependenciesAfter.getValue().isEmpty()).isTrue();

      Assertions.assertThat(serviceChosenServiceIsDependentOnRetrievedDependenciesBefore.getKey().getId().equals(ServiceChosenServiceIsDependentOnId)).isTrue();
      Assertions.assertThat(serviceChosenServiceIsDependentOnRetrievedDependenciesBefore.getValue().isEmpty()).isFalse();
      Assertions.assertThat(serviceChosenServiceIsDependentOnRetrievedDependenciesBefore.getValue().contains(selectedService)).isTrue();
      Assertions.assertThat(serviceChosenServiceIsDependentOnRetrievedDependenciesAfter.getKey().getId().equals(ServiceChosenServiceIsDependentOnId)).isTrue();
      Assertions.assertThat(serviceChosenServiceIsDependentOnRetrievedDependenciesAfter.getValue().isEmpty()).isTrue();
   }

   @Test
   void removeAllDependenciesFromService() {
      //Arrange
      List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
      ServiceEntity service1 = SampleData.getRandomizedServiceEntityWithNameNotInList(services);
      UUID service1Id = serviceRepository.save(service1);
      service1.setId(service1Id);

      /*for(ServiceEntity service : services){
         service.setId(serviceRepository.save(service));
      }*/

      services.forEach(service -> service.setId(serviceRepository.save(service)));


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
      ServiceEntity service1 = SampleData.getRandomizedServiceEntity();
      ServiceEntity service2 = SampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1));
      ServiceEntity service3 = SampleData.getRandomizedServiceEntityWithNameNotInList(List.of(service1,service2));
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
      List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
      ServiceEntity service1 = SampleData.getRandomizedServiceEntityWithNameNotInList(services);
      UUID service1Id = serviceRepository.save(service1);
      service1.setId(service1Id);

      /*for(ServiceEntity service : services){
         service.setId(serviceRepository.save(service));
      }*/

      services.forEach(service -> service.setId(serviceRepository.save(service)));

      serviceRepository.addDependencyToService(service1, services);
      //Act
      Map.Entry<ServiceEntity, List<ServiceEntity>> retrievedService =
              serviceRepository.retrieveOneWithDependencies(service1Id);
      //Assert
      Assertions.assertThat(retrievedService.getValue()).containsExactlyElementsOf(services);
      Assertions.assertThat(retrievedService.getKey()).isEqualTo(service1);
   }

   @Test
   void retrieveAllShallow() {
      //Arrange
      List<ServiceEntity> services = SampleData.getRandomLengthNonEmptyListOfServiceEntity();
      services.forEach(service -> service.setId(serviceRepository.save(service)));
      //Act
      List<ServiceEntity> retrievedServices = serviceRepository.retrieveAllServicesAndComponentsShallow();
      //Assert
      Assertions.assertThat(retrievedServices.size()).isEqualTo(services.size());
      Assertions.assertThat(retrievedServices.containsAll(services)).isTrue();
   }

   @Test
   void retrieveAllDeep() {
      //Arrange
      List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
      /*for(ServiceEntity service : services){
         service.setId(serviceRepository.save(service));
      }*/
      //TODO legge til dependencies/ gjøre assertioens motsatt vei: Det vi kontrollerer .someComparingFunction(Forventet verdi)

      services.forEach(service -> service.setId(serviceRepository.save(service)));
      //Act
      Map<ServiceEntity, List<ServiceEntity>> allRetrieved =
              serviceRepository.retrieveAllDeep();
      //Assert
      Assertions.assertThat(allRetrieved.size()).isEqualTo(services.size());
      Assertions.assertThat(allRetrieved.keySet().containsAll(services)).isTrue();

   }

   @Test
   void retrieveAllComponents() {
      //Arrange
      List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
      services.forEach(service -> {
         service.setType(ServiceType.KOMPONENT);
         service.setId(serviceRepository.save(service));
      });
      //Act
      Map<ServiceEntity, List<ServiceEntity>> retrievedComponents = serviceRepository.retrieveAllComponents();
      //Assert
      Assertions.assertThat(retrievedComponents.containsKey(services.get(0))).isTrue();
      Assertions.assertThat(retrievedComponents.keySet().size()).isEqualTo(services.size());
   }

   @Test
   void retrieveAllServices() {
      //Arrange
      List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(5);
      List <ServiceEntity> komponentTypes = new ArrayList<>();
      for(int i = 0; i < 2; i++){
         services.get(i).setType(ServiceType.KOMPONENT);
         komponentTypes.add(services.get(i));
      }
      services.forEach(service -> {
         service.setId(serviceRepository.save(service));
      });
      List <ServiceEntity> serviceTypes = new ArrayList<>();
      for(int i = 2; i < 5; i++){
         serviceTypes.add(services.get(i));
         //Assert
      }
      //Act
      Map<ServiceEntity, List<ServiceEntity>> retrievedServices = serviceRepository.retrieveAllServices();
      Assertions.assertThat(retrievedServices.keySet().size()).isEqualTo(3);
      Assertions.assertThat(retrievedServices.keySet().containsAll(serviceTypes)).isTrue();
      Assertions.assertThat(retrievedServices.keySet()).doesNotContainAnyElementsOf(komponentTypes);
   }

   @Test
   void getServicesDependantOnComponent() {
      //Arrange
      List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(5);
      List<UUID> serviceIds = new ArrayList<>();
      services.forEach(service -> {
         service.setId(serviceRepository.save(service));
         serviceIds.add(service.getId());
      });
      ServiceEntity komponent = SampleData.getRandomizedServiceEntityWithNameNotInList(services);
      UUID komponentId = serviceRepository.save(komponent);
      komponent.setId(komponentId);
      serviceIds.forEach(serviceId -> {
         serviceRepository.addDependencyToService(serviceId, komponentId);
      });
      //Act
      List<ServiceEntity> retrievedServicesDependantOnComponent = serviceRepository.getServicesDependantOnComponent(komponentId);
      //Assert
      Assertions.assertThat(retrievedServicesDependantOnComponent.containsAll(services)).isTrue();
      Assertions.assertThat(retrievedServicesDependantOnComponent.size()).isEqualTo(services.size());
   }

   @Test
   void retrieveServicesWithPolling() {
      //Arrange
      List<ServiceEntity> services = SampleData.getRandomLengthNonEmptyListOfServiceEntity();
      services.forEach(service -> {
         service.setId(serviceRepository.save(service));
      });
      //Act
      List<ServiceEntity> servicesWithPolling = serviceRepository.retrieveServicesWithPolling();
      //Assert
      Assertions.assertThat(servicesWithPolling.size()).isEqualTo(services.size());
      Assertions.assertThat(servicesWithPolling.containsAll(services)).isTrue();
   }

   @Test
   void doesEntryExist() {
      //Arrange
      UUID uuid = UUID.randomUUID();
      //Act
      Optional<ServiceEntity> shouldBeEmpty = serviceRepository.retrieve(uuid);
      //Assert
      Assertions.assertThat(shouldBeEmpty).isEmpty();


      //Arrange
      ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
      UUID uuidShouldBePresent = serviceRepository.save(serviceEntity);
      //Act
      Optional<ServiceEntity> shouldBePresent = serviceRepository.retrieve(uuidShouldBePresent);
      //Assert
      Assertions.assertThat(shouldBePresent).isNotEmpty();

   }

   @Test
   void delete() {
      //Arrange
      ServiceEntity service = SampleData.getRandomizedServiceEntity();
      UUID uuid = serviceRepository.save(service);
      service.setId(uuid);
      //Act
      serviceRepository.delete(uuid);
      Optional<ServiceEntity> shouldBeDeleted = serviceRepository.retrieve(uuid);
      //Assert
      Assertions.assertThat(shouldBeDeleted.isPresent()).isTrue();
      Assertions.assertThat(shouldBeDeleted.get().getDeleted()).isTrue();
   }


   //TODO: Skriv flere tester som denne: Orlene og Bjørg
   @Test
   void save_and_retrieve_service() {
      // Arrange
      ServiceEntity service = SampleData.getRandomizedServiceEntity();

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
              .setMonitorlink("Yes");
      ServiceEntity service2 = new ServiceEntity()
              .setName("Bye")
              .setType(ServiceType.TJENESTE)
              .setTeam("Status")
              .setMonitorlink("Yes");
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
              .setMonitorlink("Yes");
      // Act
      UUID uuid = serviceRepository.save(service);
      service.setId(uuid);
      Optional<ServiceEntity> retrievedService = serviceRepository.retrieve(uuid);

      // Assert
      Assertions.assertThat(retrievedService.orElseGet(() -> fail("klarte ikke legge til i db")))
              .isEqualTo(service);// Assert

   }


}