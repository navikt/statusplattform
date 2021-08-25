package nav.portal.core.repositories;

import nav.portal.core.entities.ExampleEntity;
import nav.portal.core.entities.RecordEntity;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class RecordRepository {
    private final DbContextTable table;

    public RecordRepository(DbContext dbContext) {
        table = dbContext.table("record");
    }

    public DatabaseSaveResult.SaveStatus save(RecordEntity entity) {
        DatabaseSaveResult<String> result = table.newSaveBuilderWithString("serviceid", entity.getServiceId())
                .setField("status", entity.getStatus())
                .setField("timestamp", entity.getTimestamp())
                .setField("responsetime", entity.getResponsetime())
                .execute();
        return result.getSaveStatus();
    }

    public Optional<RecordEntity> getLatestRecord(String serviceId) {
        return table.where("serviceid", serviceId)
                .orderBy("timestamp DESC")
                .limit(1)
                .singleObject(RecordRepository::toRecord);
    }

    private static RecordEntity toRecord(DatabaseRow row) throws SQLException {
        return new RecordEntity(row.getString("serviceid"),
                row.getString("status"),
                row.getTimestamp("timestamp"),
                row.getInt("responsetime"));
    }


    public static class Query {

        private final DbContextSelectBuilder query;

        public Query(DbContextSelectBuilder query) {
            this.query = query;
        }

        public Stream<RecordEntity> stream() {
            return query.stream(RecordRepository::toRecord);
        }

        private RecordRepository.Query query(DbContextSelectBuilder query) {
            return this;
        }
    }

}
