package nav.portal.core.repositories;

import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.RecordEntity;
import nav.portal.core.enums.ServiceStatus;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class RecordRepository {
    private final DbContextTable recordTable;
    private final DbContextTable aggregatedStatusTable;


    public RecordRepository(DbContext dbContext) {
        recordTable = dbContext.table(new DatabaseTableWithTimestamps("service_status"));
        aggregatedStatusTable = dbContext.table(new DatabaseTableWithTimestamps("daily_status_aggregation_service"));

    }

    public UUID save(RecordEntity entity) {
        DatabaseSaveResult<UUID> result = recordTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getServiceId())
                .setField("status", entity.getStatus())
                .setField("description", entity.getDescription())
                .setField("logglink", entity.getLogglink())
                .setField("response_time", entity.getResponsetime())
                .execute();

        return result.getId();
    }

    public List<DailyStatusAggregationForServiceEntity> getServiceHistoryForNumberOfDays(int number_of_days, UUID serviceId) {
        return aggregatedStatusTable.where("service_id", serviceId)
                .whereExpression("aggregation_date >= ?", ZonedDateTime.now().minusDays(number_of_days))
                .list(ServiceRepository::toDailyStatusAggregationForServiceEntity);
    }


    public Optional<DailyStatusAggregationForServiceEntity> getServiceHistoryForServiceByDate(UUID serviceId, LocalDate date) {
        return aggregatedStatusTable.where("service_id", serviceId)
                .whereExpression("aggregation_date = ?", date)
                .singleObject(ServiceRepository::toDailyStatusAggregationForServiceEntity);
    }

    public List<DailyStatusAggregationForServiceEntity> getServiceHistoryForNumberOfMonths(UUID serviceId, int number_of_months) {
        return aggregatedStatusTable.where("service_id", serviceId)
                .whereExpression("aggregation_date >= ?", ZonedDateTime.now().minusMonths(number_of_months))
                .list(ServiceRepository::toDailyStatusAggregationForServiceEntity);
    }



    public UUID saveAggregatedRecords(DailyStatusAggregationForServiceEntity entity) {
        DatabaseSaveResult<UUID> result = aggregatedStatusTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getService_id())
                .setField("number_of_status_ok", entity.getNumber_of_status_ok())
                .setField("number_of_status_issue", entity.getNumber_of_status_issue())
                .setField("number_of_status_down", entity.getNumber_of_status_down())
                .setField("aggregation_date", entity.getAggregation_date())
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

    public List<RecordEntity> getRecordsOlderThan(int daysOld){
        return recordTable
                .whereExpression("created_at <= ?", ZonedDateTime.now().minusDays(daysOld))
                .list(RecordRepository::toRecord);
    }
    public void deleteRecordsOlderThen(int daysOld) {
        recordTable.whereExpression("created_at <= ?", ZonedDateTime.now().minusDays(daysOld))
                .executeDelete();
    }


    private static RecordEntity toRecord(DatabaseRow row) throws SQLException {
        return new RecordEntity()
                .setId(row.getUUID("id"))
                .setServiceId(row.getUUID("service_id"))
                .setDescription(row.getString("description"))
                .setLogglink(row.getString("logglink"))
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
