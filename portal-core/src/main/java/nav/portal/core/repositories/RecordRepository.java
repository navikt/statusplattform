package nav.portal.core.repositories;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.enums.ServiceStatus;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class RecordRepository {
    private final DbContextTable recordTable;


    public RecordRepository(DbContext dbContext) {
        //TODO:Vurder skal Record bytte navn til service_status
        recordTable = dbContext.table(new DatabaseTableWithTimestamps("service_status"));
    }

    public UUID save(RecordEntity entity) {
        DatabaseSaveResult<UUID> result = recordTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getServiceId())
                .setField("status", entity.getStatus())
                .setField("response_time", entity.getResponsetime())
                .execute();
        return result.getId();
    }

    public Optional<RecordEntity> getLatestRecord(UUID serviceId) {
        return recordTable.where("service_id", serviceId)
                .orderBy("created_at DESC")
                .limit(1)
                .singleObject(RecordRepository::toRecord);
    }

    //TODO denne skal bli paginert
    public List<RecordEntity> getRecordHistory(UUID serviceId, int maxNumberOfRecords) {
        return recordTable.where("service_id", serviceId)
                .orderBy("created_at DESC")
                .limit(maxNumberOfRecords)
                .list(RecordRepository::toRecord);
    }

    private static RecordEntity toRecord(DatabaseRow row) throws SQLException {
        return new RecordEntity()
                .setId(row.getUUID("id"))
                .setServiceId(row.getUUID("service_id"))
                .setStatus(ServiceStatus.fromDb(row.getString("status")).orElse(ServiceStatus.ISSUE))
                .setCreated_at(row.getZonedDateTime("created_at"))
                .setResponsetime(row.getInt("response_time"));
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
