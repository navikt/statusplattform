package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.RecordDeltaEntity;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.RecordSource;
import nav.statusplattform.core.enums.ServiceStatus;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RecordRepository {
    private final DbContextTable recordTable;
    private final DbContextTable recordDeltaTable;
    private final DbContextTable serviceTable;


    public RecordRepository(DbContext dbContext) {
        serviceTable = dbContext.table(new DatabaseTableWithTimestamps("service"));
        recordTable = dbContext.table(new DatabaseTableWithTimestamps("service_status"));
        recordDeltaTable = dbContext.table(new DatabaseTableWithTimestamps("service_status_delta"));

    }

    public UUID save(RecordEntity entity) {
        RecordSource source = entity.getRecordSource() != null? entity.getRecordSource(): RecordSource.UNKNOWN;
        DatabaseSaveResult<UUID> result = recordTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getServiceId())
                .setField("status", entity.getStatus())
                .setField("description", entity.getDescription())
                .setField("logglink", entity.getLogglink())
                .setField("response_time", entity.getResponsetime())
                .setField("source", source)
                .execute();

        return result.getId();
    }

    //UUIDen som settes her skal IKKE generes, men settes fra uid fra orginal record.
    public UUID saveNewStatusDiff(RecordEntity entity) {
        DatabaseSaveResult<UUID> result = recordDeltaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getServiceId())
                .setField("status", entity.getStatus())
                .setField("active", entity.getActive())
                .setField("counter", 1) // Når denne metoden brukes, skal det være første gang diff lagres, og counter skal være 1
                .execute();
        return result.getId();
    }

    public void setOldStatusDiffInactive(RecordDeltaEntity entity) {

        recordDeltaTable.where("service_id", entity.getServiceId())
                .where("active",true).update()
                .setField("active", false)
                .execute();

    }

    public void increaseCountOnStatusDiff(RecordDeltaEntity entity) {
        recordDeltaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("counter", entity.getCounter() + 1)
                .execute();

    }

    public Optional<RecordDeltaEntity> getLatestRecordDiff(UUID serviceId) {
        return recordDeltaTable.where("service_id", serviceId)
                .orderBy("created_at DESC")
                .limit(1)
                .singleObject(RecordRepository::toRecordDelta);
    }

    public Optional<RecordDeltaEntity> getActiveRecordDelta(UUID serviceId){
        return recordDeltaTable.where("service_id", serviceId)
                .where("active",true)
                .singleObject(RecordRepository::toRecordDelta);
    }

    public Optional<RecordEntity> getLatestRecord(UUID serviceId) {
        return recordTable.where("service_id", serviceId)
                .orderBy("created_at DESC")
                .limit(1)
                .singleObject(RecordRepository::toRecord);
    }

    public java.util.Map<UUID, RecordEntity> getLatestRecordsForServices(List<UUID> serviceIds) {
        if (serviceIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        // Get all records for the given service IDs and group by service_id
        List<RecordEntity> allRecords = recordTable
                .whereIn("service_id", serviceIds)
                .orderBy("created_at DESC")
                .list(RecordRepository::toRecord);

        // Keep only the latest record for each service_id
        java.util.Map<UUID, RecordEntity> latestRecordsMap = new java.util.HashMap<>();
        for (RecordEntity record : allRecords) {
            latestRecordsMap.putIfAbsent(record.getServiceId(), record);
        }

        return latestRecordsMap;
    }

    public java.util.Map<UUID, RecordDeltaEntity> getLatestRecordDiffsForServices(List<UUID> serviceIds) {
        if (serviceIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        // Get all record diffs for the given service IDs and group by service_id
        List<RecordDeltaEntity> allRecordDiffs = recordDeltaTable
                .whereIn("service_id", serviceIds)
                .orderBy("created_at DESC")
                .list(RecordRepository::toRecordDelta);

        // Keep only the latest record diff for each service_id
        java.util.Map<UUID, RecordDeltaEntity> latestRecordDiffsMap = new java.util.HashMap<>();
        for (RecordDeltaEntity recordDiff : allRecordDiffs) {
            latestRecordDiffsMap.putIfAbsent(recordDiff.getServiceId(), recordDiff);
        }

        return latestRecordDiffsMap;
    }

    //TODO denne skal bli paginert
    public List<RecordEntity> getRecordHistory(UUID serviceId, int maxNumberOfRecords) {
        return recordTable.where("service_id", serviceId)
                .orderBy("created_at DESC")
                .limit(maxNumberOfRecords)
                .list(RecordRepository::toRecord);
    }

    public final List<RecordEntity> getRecordsInTimeSpan(UUID serviceId, LocalDateTime from, LocalDateTime to) {
        List<RecordEntity> recordsInTimeSpan = recordDeltaTable.where("service_id", serviceId)
                .whereExpression("created_at >= ?", from.atZone(ZoneId.systemDefault()))
                .whereExpression("created_at <= ?", to.atZone(ZoneId.systemDefault()))
                .orderBy("created_at ASC")
                .list(RecordRepository::toRecordFromDelta);


        try {

            Optional<RecordEntity> recordInTimeSpan = recordDeltaTable.where("service_id", serviceId)
                    .whereExpression("created_at <= ?", from)
                    .orderBy("created_at DESC")
                    .limit(1)
                    .singleObject(RecordRepository::toRecordFromDelta);

            //RecordEntity firstRecordInTimeSpan = recordInTimeSpan.orElseThrow();
            //if the dateentry from time is greater than database , we replace the last record with the new from date entry.
            if (recordInTimeSpan.isPresent()) {
                // Create a new RecordEntity using the 'from' time
                RecordEntity newRecord = new RecordEntity();
                newRecord.setId(UUID.randomUUID()); // Generate a new UUID or use an appropriate value
                newRecord.setServiceId(serviceId);
                newRecord.setCreated_at(from.atZone(ZoneId.systemDefault())); // Use 'from' as the created_at time
                newRecord.setStatus(recordInTimeSpan.get().getStatus()); // Use the status from the existing record
                recordsInTimeSpan.addFirst(newRecord); // Add the new record at the beginning of the list
            }
            //recordsInTimeSpan.addFirst(firstRecordInTimeSpan);
        } catch (Exception e) {
            return recordsInTimeSpan;
        }

        return recordsInTimeSpan;
    }

    public List<RecordEntity> getAllRecordsFromYesterday(){
        ZonedDateTime yesterdayMidnight = ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime todayMidnight = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);

        return recordTable
                .whereExpression("created_at <= ?", todayMidnight )
                .whereExpression("created_at >= ?", yesterdayMidnight)
                .list(RecordRepository::toRecord);
    }
    public ZonedDateTime getLatestGcpPollTime(){
        Optional<ServiceEntity> optionalServiceEntity = serviceTable.where("polling_on_prem", false)
                .whereExpression("polling_url is NOT null").stream(ServiceRepository::toService).findFirst();
        if(optionalServiceEntity.isPresent()){
            Optional<RecordEntity> latest = getLatestRecord(optionalServiceEntity.get().getId());
            return latest.map(RecordEntity::getCreated_at).orElse(null);
        }
        return null;
    }

    public ZonedDateTime getLatestFssPollTime(){
        Optional<ServiceEntity> optionalServiceEntity = serviceTable.where("polling_on_prem", true)
                .whereExpression("polling_url is NOT null").stream(ServiceRepository::toService).findFirst();
        if(optionalServiceEntity.isPresent()){
            Optional<RecordEntity> latest = getLatestRecord(optionalServiceEntity.get().getId());
            return latest.map(RecordEntity::getCreated_at).orElse(null);
        }
        return null;
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

    public static UUID saveRecordBackInTime(RecordEntity entity, DbContext dbContext) {
        DbContextTable recordTable = dbContext.table(new DatabaseTableImpl("service_status"));
        DatabaseSaveResult<UUID> result = recordTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getServiceId())
                .setField("created_at", entity.getCreated_at())
                .setField("status", entity.getStatus())
                .setField("description", entity.getDescription())
                .setField("logglink", entity.getLogglink())
                .setField("response_time", entity.getResponsetime())
                .execute();
        return result.getId();
    }



    private static RecordEntity toRecord(DatabaseRow row) throws SQLException {
        return new RecordEntity()
                    .setId(row.getUUID("id"))
                    .setServiceId(row.getUUID("service_id"))
                    .setDescription(row.getString("description"))
                    .setLogglink(row.getString("logglink"))
                    .setStatus(ServiceStatus.fromDb(row.getString("status")).orElse(ServiceStatus.ISSUE))
                    .setCreated_at(row.getZonedDateTime("created_at"))
                    .setResponsetime(row.getInt("response_time"))
                    .setRecordSource(RecordSource.fromDb(row.getString("source")).orElse(RecordSource.UNKNOWN));
    }


    private static RecordDeltaEntity toRecordDelta(DatabaseRow row) throws SQLException {
        return new RecordDeltaEntity()
                .setId(row.getUUID("id"))
                .setServiceId(row.getUUID("service_id"))
                .setCounter(row.getInt("counter"))
                .setActive(row.getBoolean("active"))
                .setStatus(ServiceStatus.fromDb(row.getString("status")).orElse(ServiceStatus.ISSUE))
                .setUpdated_at(row.getZonedDateTime("updated_at"))
                .setCreated_at(row.getZonedDateTime("created_at"));
    }

    public void deleteRecords(List<RecordEntity> records) {
        recordTable.whereIn("id", records.stream().map(RecordEntity::getId).collect(Collectors.toList()))
                .executeDelete();

    }

    public void deleteRecordsOlderThan48hours() {
        recordTable.whereExpression("created_at <= ?", ZonedDateTime.now().minusHours(48))
                .executeDelete();
    }

    private static RecordEntity toRecordFromDelta(DatabaseRow row) throws SQLException {
        return new RecordEntity()
                .setId(row.getUUID("id"))
                .setServiceId(row.getUUID("service_id"))
                .setStatus(ServiceStatus.fromDb(row.getString("status")).orElse(ServiceStatus.ISSUE))
                .setCreated_at(row.getZonedDateTime("created_at"))
                .setResponsetime(200)          // Default or null value as it's not in RecordDeltaEntity
                .setDescription(null)          // Default or null value
                .setLogglink(null)             // Default or null value
                .setRecordSource(null); // Set RecordSource to DELTA
    }
}
