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
      connection.close();
   }

   private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);




   @Test
   void save() {
   }

   @Test
   void update() {
   }

   @Test
   void retrieve() {
   }

   @Test
   void addDependencyToService() {
   }

   @Test
   void testAddDependencyToService() {
   }

   @Test
   void removeDependencyFromService() {
   }

   @Test
   void removeAllDependenciesFromService() {
   }

   @Test
   void isOtherServicesDependentOn() {
   }

   @Test
   void retrieveOneWithDependencies() {
   }

   @Test
   void retriveAll() {
   }

   @Test
   void doesEntryExist() {
   }

   @Test
   void testRetrieve() {
   }

   @Test
   void delete() {
   }

   @Test
   void toService() {
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
      // Arrange
      ServiceEntity service = sampleData.getRandomizedServiceEntity();
      List<ServiceEntity> dependentServices = List.of(sampleData.getRandomizedServiceEntity());

      // Act
      serviceRepository.save(service);
      dependentServices.forEach(serviceRepository::save);
      serviceRepository.addDependencyToService(service, dependentServices);

      // Assert
      Map.Entry<ServiceEntity,List<ServiceEntity>> retrievedService = serviceRepository.retrieveOneWithDependencies(service.getId());
      List<ServiceEntity> retrievedDependentServices = retrievedService.getValue();
      Assertions.assertThat(retrievedDependentServices).isEqualTo(dependentServices);

   }

   @Test
   void save_and_retrieve_service_2() {
      // Arrange
      /*    private String name;
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