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
      ServiceEntity service = new ServiceEntity("a", null, ServiceType.APPLIKASJON, "team", "monitor", "description", "logglink");
      UUID uuid = repository.save(service);

      service.setId(uuid);
      Assertions.assertThat(repository.retrieve(uuid).orElseGet(() -> fail("klarte ikke legge til i db")))
              .isEqualTo(service);
   }

   /*
      TODO:
      2: Flere felter er omdøpte, må rettes til korrekt tye og navn SE ETTER NORSKE NAVN
      3: Services har avhengighet til andre services, må modelleres korrekt (via egen koblingstabell)
      4: Enumer, lag enumene i koden
      5: Lage testdatabuilder, se prodregprosjekt.
      6: Skrive tester.
      7: Fikse yaml kontrakt: 
    */


}