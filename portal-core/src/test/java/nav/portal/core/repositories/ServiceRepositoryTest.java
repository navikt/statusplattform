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

   private final ServiceRepository repository = new ServiceRepository(dbContext);

   @Test
   void retrieve_RetrievesOne() {
      // Arrange
      ServiceEntity service = sampleData.getServiceEntity();

      // Act
      UUID uuid = repository.save(service);
      service.setId(uuid);

      // Assert
      Assertions.assertThat(repository.retrieve(uuid).orElseGet(() -> fail("klarte ikke legge til i db")))
              .isEqualTo(service);
   }





}