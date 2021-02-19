package nav.portal.core.repositories;

import nav.portal.core.entities.ExampleEntity;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DatabaseSaveResult;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleRepositoryTest {

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

   private final ExampleRepository repository = new ExampleRepository(dbContext);

   @Test
   void retrieve_RetrievesOne() {
      ExampleEntity entity = sampleData.sampleExample();
      DatabaseSaveResult.SaveStatus status = repository.save(entity);

      assertThat(status).isEqualTo(DatabaseSaveResult.SaveStatus.INSERTED);
      Assertions.assertThat(repository.retrieve(entity.getUid()))
            .isEqualTo(entity);
   }

}