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
    private final DbContextTable dashboardAreaTable;

    public AreaRepository(DbContext dbContext) {
        areaTable = dbContext.table("area");
        areaServiceTable = dbContext.table("area_service");
        serviceTable = dbContext.table("service");
        dashboardAreaTable = dbContext.table("dashboard_area");
    }

    public UUID save(AreaEntity entity) {
        DatabaseSaveResult<UUID> result = areaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("name",entity.getName())
                .setField("description", entity.getDescription())
                .setField("icon", entity.getIcon())
                .execute();
        return result.getId();
    }
    public DatabaseSaveResult.SaveStatus uppdate(AreaEntity entity) {
        DatabaseSaveResult<UUID> result = areaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("name",entity.getName())
                .setField("description", entity.getDescription())
                .setField("icon", entity.getIcon())
                .execute();
        return result.getSaveStatus();
    }


    public boolean deleteArea(UUID areaId){
        if(areaTable.where("id",areaId).singleObject(AreaRepository::toArea).isEmpty()){
            return false;
        }
        dashboardAreaTable.where("area_id", areaId).executeDelete();
        areaServiceTable.where("area_id", areaId).executeDelete();
        areaTable.where("id", areaId).executeDelete();
        return true;

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
    //TODO bør denne være optional?
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

    public List<AreaEntity> retriveAllShallow(){
        return areaTable.orderedBy("name").stream(AreaRepository::toArea).collect(Collectors.toList());
    }
    //TODO bør denne være optional?
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
                    row.getString("description"),
                    row.getString("icon"));
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
