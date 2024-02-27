package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.AreaEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.entities.SubAreaEntity;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import org.actioncontroller.HttpRequestException;
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
    private final DbContextTable subAreaTable;
    private final DbContextTable areaSubAreaTable;


    public AreaRepository(DbContext dbContext) {
        areaTable = dbContext.table("area");
        areaServiceTable = dbContext.table("area_service");
        serviceTable = dbContext.table("service");
        dashboardAreaTable = dbContext.table("dashboard_area");
        subAreaTable = dbContext.table("sub_area");
        areaSubAreaTable = dbContext.table("area_sub_area");
    }

    public UUID save(AreaEntity entity) {
        //Sjekk på navn
        if(areaTable.where("name",entity.getName()).getCount()>0) {
            throw new HttpRequestException("Område med navn: "+ entity.getName() +" finnes allerede");
        }
        DatabaseSaveResult<UUID> result = areaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("name",entity.getName())
                .setField("description", entity.getDescription())
                .setField("contains_components", entity.getContains_components())
                .execute();
        return result.getId();
    }
    public DatabaseSaveResult.SaveStatus update(AreaEntity entity) {
        DatabaseSaveResult<UUID> result = areaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("name",entity.getName())
                .setField("description", entity.getDescription())
                .setField("contains_components", entity.getContains_components())
                .execute();
        return result.getSaveStatus();
    }

    public void updateArea(AreaEntity entity) {
        areaTable.where("id", entity.getId()).update()
                .setFieldIfPresent("name", entity.getName())
                .setField("description", entity.getDescription())
                .setField("contains_components", entity.getContains_components())
                .execute();
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

    public void addSubAreaToArea(UUID areaId, UUID subAreaId) {
        areaSubAreaTable.insert()
                .setField("area_id", areaId)
                .setField("sub_area_id", subAreaId)
                .execute();
    }

    public void addSubAreaToArea(UUID areaId, List<UUID> subAreaId) {
        areaSubAreaTable.where("area_id", areaId).executeDelete();
        for(UUID subareaId: subAreaId) {
            areaSubAreaTable.insert()
                    .setField("area_id", areaId)
                    .setField("sub_area_id", subareaId)
                    .execute();
        }
    }

    public void addServiceToAreas(List<UUID> areaIds, UUID serviceId) {
        for(UUID areaId: areaIds) {
            areaServiceTable.insert()
                    .setField("area_id", areaId)
                    .setField("service_id", serviceId)
                    .execute();
        }
    }

    public void setServicesOnArea(UUID areaId, List<UUID> services) {
        areaServiceTable.where("area_id", areaId).executeDelete();

        for(int i = 0; i < services.size(); i++){
            areaServiceTable.insert()
                    .setField("area_id", areaId)
                    .setField("service_id", services.get(i))
                    .execute();
        }

    }


    public void removeServiceFromAllAreas(UUID serviceId){
        areaServiceTable.where("service_id",serviceId)
                .executeDelete();

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

    public List<AreaEntity> retriveAllShallow(){
        return areaTable.orderedBy("name")
                .stream(AreaRepository::toArea)
                .collect(Collectors.toList());
    }

    public List<AreaEntity> retriveAllWithComponentsShallow(){
        return areaTable.orderedBy("name")
                .where("contains_components",true)
                .stream(AreaRepository::toArea)
                .collect(Collectors.toList());
    }

    public Map<AreaEntity, List<ServiceEntity>> retrieveAll() {
        DbContextTableAlias areaAlias = areaTable.alias("area");
        DbContextTableAlias a2s = areaServiceTable.alias("a2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<AreaEntity, List<ServiceEntity>> result = new HashMap<>();
        areaAlias
                .leftJoin(areaAlias.column("id"), a2s.column("area_id"))
                .leftJoin(a2s.column("service_id"), service.column("id"))
                .orderBy(areaAlias.column("name"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toArea(row.table(areaAlias)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        result.values().forEach(serviceList -> serviceList.sort(Comparator.comparing(s -> s.getName().toLowerCase(Locale.ROOT))));
        result.entrySet()
                .forEach(entry -> entry.setValue(entry.getValue().stream().filter(s -> s.getDeleted().equals(false))
                        .collect(Collectors.toList())));
        return result;
    }

    public List<SubAreaEntity> getSubAreasOnArea(UUID areaID) {
        List<SubAreaEntity> subareasOnArea = new ArrayList<>();

        DbContextTableAlias sa = subAreaTable.alias("sa");
        DbContextTableAlias a2sa = areaSubAreaTable.alias("a2sa");

        a2sa.where("area_id", areaID)
                .leftJoin(a2sa.column("sub_area_id"),sa.column("id"))
                .list(row -> subareasOnArea.add(SubAreaRepository.toSubArea(row.table(sa))));
        return subareasOnArea;

    }



    static AreaEntity toArea(DatabaseRow row){
        try {
            return new AreaEntity(row.getUUID("id"),
                    row.getString("name"),
                    row.getString("description"),
                    row.getBoolean("contains_components"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    public Query query() {
        return new Query(areaTable.query());
    }

    public List<AreaEntity> getAreasContainingService(UUID service_id) {
        DbContextTableAlias areaAlias = areaTable.alias("area");
        DbContextTableAlias a2s = areaServiceTable.alias("a2s");
        return  areaAlias
                .leftJoin(areaAlias.column("id"), a2s.column("area_id"))
                .orderBy(areaAlias.column("name"))
                .where("a2s.service_id",service_id)
                .stream(AreaRepository::toArea).collect(Collectors.toList());
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
