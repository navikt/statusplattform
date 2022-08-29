package nav.portal.core.repositories;

import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.RecordEntity;
import nav.portal.core.enums.ServiceStatus;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecordRepository {
    private final DbContextTable recordTable;
    private final DbContextTable recordDiffTable;
    private final DbContextTable aggregatedStatusTable;


    public RecordRepository(DbContext dbContext) {
        recordTable = dbContext.table(new DatabaseTableWithTimestamps("service_status"));
        aggregatedStatusTable = dbContext.table(new DatabaseTableWithTimestamps("daily_status_aggregation_service"));
        recordDiffTable = dbContext.table(new DatabaseTableWithTimestamps("service_status_delta"));

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

    //UUIDen som settes her skal IKKE generes, men settes fra uid fra orginal record.
    public UUID saveNewStatusDiff(RecordEntity entity) {
        DatabaseSaveResult<UUID> result = recordDiffTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getServiceId())
                .setField("status", entity.getStatus())
                .setField("description", entity.getDescription())
                .setField("active", entity.getActive())
                .setField("logglink", entity.getLogglink())
                .setField("response_time", entity.getResponsetime())
                .setField("counter", 1) // Når denne metoden brukes, skal det være første gang diff lagres, og counter skal være 1
                .execute();
        return result.getId();
    }

    public UUID saveOldStatusDiff(RecordEntity entity) {
        DatabaseSaveResult<UUID> result = recordDiffTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("active", false)
                .setField("counter", entity.getCounter())
                .execute();
        return result.getId();
    }

    public void increaseCountOnStatusDiff(RecordEntity entity) {
        recordDiffTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("counter", entity.getCounter() + 1)
                .execute();

    }
    public Optional<RecordEntity> getLatestRecordDiff(UUID serviceId) {
        return recordDiffTable.where("service_id", serviceId)
                .orderBy("created_at DESC")
                .limit(1)
                .singleObject(RecordRepository::toRecord);
    }

    public Optional<RecordEntity> getLatestRecordDiffBeforeDate(UUID serviceId, LocalDate date) {
        return recordDiffTable.where("service_id", serviceId)
                .whereExpression("created_at <= ?", LocalDateTime.of(date, LocalTime.of(0,0)))
                .orderBy("created_at DESC")
                .limit(1)
                .singleObject(RecordRepository::toRecord);
    }

    public List<DailyStatusAggregationForServiceEntity> getServiceHistoryForNumberOfDays(int number_of_days, UUID serviceId) {
        return aggregatedStatusTable.where("service_id", serviceId)
                .whereExpression("aggregation_date >= ?", LocalDate.now().minusDays(number_of_days))
                .list(ServiceRepository::toDailyStatusAggregationForServiceEntity);
    }


    public Optional<DailyStatusAggregationForServiceEntity> getServiceHistoryForServiceByDate(UUID serviceId, LocalDate date) {
        return aggregatedStatusTable.where("service_id", serviceId)
                .whereExpression("aggregation_date = ?", date)
                .singleObject(ServiceRepository::toDailyStatusAggregationForServiceEntity);
    }

    public List<DailyStatusAggregationForServiceEntity> getServiceHistoryForNumberOfMonths(UUID serviceId, int number_of_months) {
        return aggregatedStatusTable.where("service_id", serviceId)
                .whereExpression("aggregation_date >= ?", LocalDate.now().minusMonths(number_of_months))
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


    public Map<UUID, Map<LocalDate,List<RecordEntity>>> getAllRecordsOrderedByServiceIdAndDate(){

        List<RecordEntity> allRecords = recordTable.unordered().list(RecordRepository::toRecord);
        List<UUID> serviceUUIDs = allRecords.stream().map(RecordEntity::getServiceId).distinct().collect(Collectors.toList());
        Map<UUID, Map<LocalDate,List<RecordEntity>>> result = new HashMap<>();
        serviceUUIDs.forEach(uuid -> {
            List<RecordEntity> recordsForService = allRecords.stream().filter(r -> r.getServiceId().equals(uuid)).collect(Collectors.toList());
            List<LocalDate> dates = recordsForService.stream().map(r -> r.getCreated_at().toLocalDate()).distinct().collect(Collectors.toList());
            Map<LocalDate,List<RecordEntity>> resultForOneService = new HashMap<>();
            dates.forEach(date -> {
                resultForOneService.put(date,recordsForService.stream().filter(r -> r.getCreated_at().toLocalDate().equals(date)).collect(Collectors.toList()));
            });
            result.put(uuid,resultForOneService);

        });
        return result;

    }


    public List<RecordEntity> getAllRecordsFromYesterday(){
        ZonedDateTime yesterdayMidnight = ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime todayMidnight = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);

        return recordTable
                .whereExpression("created_at <= ?", todayMidnight )
                .whereExpression("created_at >= ?", yesterdayMidnight)
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
    public void deleteRecordsForDate(UUID serviceID,LocalDate date) {
        recordTable.where("id", serviceID)
                .whereExpression("created_at >= ?", date.minusDays(1))
                .whereExpression("created_at <= ?", date)
                .executeDelete();
    }



    private static RecordEntity toRecord(DatabaseRow row) throws SQLException {
        Integer counter;
        Boolean active;
        try{
            counter = row.getInt("counter");
            active = row.getBoolean("active");

        }
        catch (IllegalArgumentException e){
            counter = null;
            active = false;
        }
        return new RecordEntity()
                    .setId(row.getUUID("id"))
                    .setServiceId(row.getUUID("service_id"))
                    .setDescription(row.getString("description"))
                    .setCounter(counter)
                    .setActive(active)
                    .setLogglink(row.getString("logglink"))
                    .setStatus(ServiceStatus.fromDb(row.getString("status")).orElse(ServiceStatus.ISSUE))
                    .setCreated_at(row.getZonedDateTime("created_at"))
                    .setResponsetime(row.getInt("response_time"));


    }

    public void deleteRecords(List<RecordEntity> records) {
        recordTable.whereIn("id", records.stream().map(RecordEntity::getId).collect(Collectors.toList()))
                .executeDelete();

    }

    public void deleteRecordsOlderThen48hours() {
        recordTable.whereExpression("created_at <= ?", ZonedDateTime.now().minusHours(48))
                .executeDelete();
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
