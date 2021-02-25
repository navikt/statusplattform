package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.ExampleEntity;

import nav.portal.core.repositories.ExampleRepository;
import nav.portal.core.repositories.SampleData;
import nav.portal.core.repositories.TestDataSource;
//import no.portal.web.generated.api.ExampleDataDto;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ExampleControllerTest {

   private final SampleData sampleData = new SampleData();
   private final DataSource dataSource = TestDataSource.create();
   private final DbContext dbContext = new DbContext();

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
   private final AreaController controller = new AreaController(dbContext);


   @Test
   public void getGroup_RetrievesSavedGroup() {
      /***
      //arrange
      ExampleEntity entity = sampleData.sampleExample();
      repository.save(entity);

      //act
      ExampleDataDto dto = controller.getData(entity.getUid());

      //assert
      assertThat(dto).isEqualTo(AreaController.toExampleDto(entity));
       **/
   }


}
