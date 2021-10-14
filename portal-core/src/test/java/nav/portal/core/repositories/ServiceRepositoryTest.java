package nav.portal.core.repositories;

import nav.portal.core.entities.ServiceEntity;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.security.Provider;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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



   //TODO: Skriv flere tester som denne: Orlene og Bjørg
   @Test
   void retrieve_RetrievesOne() {
      // Arrange
      ServiceEntity service = sampleData.getServiceEntity();

      // Act
      UUID uuid = serviceRepository.save(service);

      // Assert
      Assertions.assertThat(serviceRepository.retrieve(uuid).orElseGet(() -> fail("klarte ikke legge til i db")))
              .isEqualTo(service);
   }

   //Skrive test for å legge til avhengigheter:

   @Test
   void save_and_confirm_dependencies() {
      // Arrange
      ServiceEntity service = sampleData.getServiceEntity();
      List<ServiceEntity> dependentServices = List.of(sampleData.getServiceEntity());

      // Act
      serviceRepository.save(service);
      dependentServices.forEach(serviceRepository::save);

      /*
      for(int i= 0; i < dependantService.size(); i++){
         serviceRepository.save(dependantService.get(i));
      }*/

      serviceRepository.addDependenciesToService(service, dependentServices);

      // Assert
      Map.Entry<ServiceEntity,List<ServiceEntity>> retrievedService = serviceRepository.retrieveOneWithDependencies(service.getId());
      List<ServiceEntity> retrievedDependentServices = retrievedService.getValue();
      Assertions.assertThat(retrievedDependentServices).isEqualTo(dependentServices);

   }









}