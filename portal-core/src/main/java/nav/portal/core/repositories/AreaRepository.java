package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.exceptionHandling.ExceptionUtil;
import org.fluentjdbc.*;



import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;




public class AreaRepository {

    private final DbContextTable areaTable;
    private final DbContextTable areaServiceTable;
    private final DbContextTable serviceTable;

    public AreaRepository(DbContext dbContext) {
        areaTable = dbContext.table("area");
        areaServiceTable = dbContext.table("area_service");
        serviceTable = dbContext.table("service");
    }

    public DatabaseSaveResult.SaveStatus save(AreaEntity entity) {
        DatabaseSaveResult<UUID> result = areaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("name",entity.getName())
                .setField("beskrivelse", entity.getBeskrivelse())
                .setField("ikon", entity.getIkon())
                .execute();
        return result.getSaveStatus();
    }
    public DatabaseSaveResult.SaveStatus uppdate(AreaEntity entity) {
        DatabaseSaveResult<UUID> result = areaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("name",entity.getName())
                .setField("beskrivelse", entity.getBeskrivelse())
                .setField("ikon", entity.getIkon())
                .execute();
        return result.getSaveStatus();
    }



    public void addServiceToArea(UUID areaId, UUID serviceId) {
        areaServiceTable.insert()
                .setField("area_id", areaId)
                .setField("service_id", serviceId)
                .execute();
    }

    public void removeServiceFromArea(UUID areaId, UUID serviceId) {
        areaServiceTable.where("area_id",areaId)
                .where("service_id",serviceId)
                .executeDelete();

    }

    public Map.Entry<AreaEntity, List<ServiceEntity>> retrieveOne(UUID area_id) {
        DbContextTableAlias areaAlias = areaTable.alias("area");
        DbContextTableAlias a2s = areaServiceTable.alias("a2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<AreaEntity, List<ServiceEntity>> result = new HashMap<>();
        areaAlias
                .where("id" , area_id)
                .leftJoin(areaAlias.column("id"), a2s.column("area_id"))
                .leftJoin(a2s.column("service_id"), service.column("id"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toArea(row.table(areaAlias)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        return result
                .entrySet()
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: Area with id " + area_id));
    }

    public Map<AreaEntity, List<ServiceEntity>> retrieveAll() {
        DbContextTableAlias areaAlias = areaTable.alias("area");
        DbContextTableAlias a2s = areaServiceTable.alias("a2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<AreaEntity, List<ServiceEntity>> result = new HashMap<>();
        areaAlias
                .leftJoin(areaAlias.column("id"), a2s.column("area_id"))
                .leftJoin(a2s.column("service_id"), service.column("id"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toArea(row.table(areaAlias)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        return result;
    }

    static AreaEntity toArea(DatabaseRow row){
        try {
            return new AreaEntity(row.getUUID("id"),
                    row.getString("name"),
                    row.getString("beskrivelse"),
                    row.getString("ikon"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    public Query query() {
        return new Query(areaTable.query());
    }

    public static class Query {

        private final DbContextSelectBuilder query;

        public Query(DbContextSelectBuilder query) {
            this.query = query;
        }

        public Stream<AreaEntity> stream() {
            return query.stream(AreaRepository::toArea);
        }

        private Query query(DbContextSelectBuilder query) {
            return this;
        }
    }
}
