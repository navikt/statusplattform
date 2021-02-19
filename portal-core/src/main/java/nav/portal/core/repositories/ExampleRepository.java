package nav.portal.core.repositories;


import nav.portal.core.entities.ExampleEntity;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

public class ExampleRepository {

   private final DbContextTable table;

   public ExampleRepository(DbContext dbContext) {
      table = dbContext.table("ExampleEntity");
   }

   public DatabaseSaveResult.SaveStatus save(ExampleEntity entity) {
      DatabaseSaveResult<UUID> result = table.newSaveBuilderWithUUID("uid", entity.getUid())
            .setField("code", entity.getCode())


            .execute();
      return result.getSaveStatus();
   }

   public ExampleEntity retrieve(UUID exampleUid) {
      return table.where("uid", exampleUid)
            .singleObject(ExampleRepository::toGroup)
            .orElseThrow(() -> new IllegalArgumentException("Not found: ExampleEntity with groupUid " + exampleUid));
   }

   private static ExampleEntity toGroup(DatabaseRow row) throws SQLException {
      return new ExampleEntity()
            .setUid(row.getUUID("uid"))
            .setCode(row.getInt("code"));
   }

   public Query query() {
      return new Query(table.query());
   }

   public static class Query {

      private final DbContextSelectBuilder query;

      public Query(DbContextSelectBuilder query) {
         this.query = query;
      }

      public Stream<ExampleEntity> stream() {
         return query.stream(ExampleRepository::toGroup);
      }

      private Query query(DbContextSelectBuilder query) {
         return this;
      }
   }
}
