package nav.portal.core.repositories;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.entities.SubAreaEntity;
import nav.portal.core.exceptionHandling.ExceptionUtil;
import org.actioncontroller.HttpRequestException;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SubAreaRepository {

    private final DbContextTable areaTable;
    private final DbContextTable subAreaTable;
    private final DbContextTable areaSubAreaTable;
    private final DbContextTable subAreaServiceTable;
    private final DbContextTable areaServiceTable;
    private final DbContextTable serviceTable;

    public SubAreaRepository(DbContext dbContext) {
        areaTable = dbContext.table("area");
        subAreaTable = dbContext.table("sub_area");
        areaSubAreaTable = dbContext.table("area_sub_area");
        subAreaServiceTable = dbContext.table("sub_area_service");
        areaServiceTable = dbContext.table("area_service");
        serviceTable = dbContext.table("service");
    }

    public UUID save(SubAreaEntity entity) {
        //Sjekk på navn
        if(subAreaTable.where("name",entity.getName()).getCount()>0) {
            throw new HttpRequestException("Sub-område med navn: "+ entity.getName() +" finnes allerede");
        }
        DatabaseSaveResult<UUID> result = subAreaTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("name",entity.getName())
                .execute();
        return result.getId();
    }

    public void updateSubArea(SubAreaEntity subAreaEntity) {
        subAreaTable.where("id", subAreaEntity.getId()).update()
                .setField("name", subAreaEntity.getName())
                .execute();
    }


    public boolean deleteSubArea(UUID subAreaId){
        if(subAreaTable.where("id", subAreaId).singleObject(SubAreaRepository::toSubArea).isEmpty()){
            return false;
        }
        areaSubAreaTable.where("sub_area_id", subAreaId).executeDelete();
        subAreaTable.where("id", subAreaId).executeDelete();
        return true;

    }

    public List<ServiceEntity> getServicesOnSubArea(UUID subAreaID) {
        DbContextTableAlias subAreaAlias = subAreaTable.alias("subAreaAlias");
        DbContextTableAlias subAreaServiceAlias = subAreaServiceTable.alias("subAreaServiceAlias");
        DbContextTableAlias serviceAlias = serviceTable.alias("serviceAlias");

        List<ServiceEntity> result = new ArrayList<>();

        serviceAlias
                .leftJoin(serviceAlias.column("id"),subAreaServiceAlias.column("service_id"))
                .leftJoin(subAreaServiceAlias.column("sub_area_id"),subAreaAlias.column("id"))
                .where("subAreaAlias.id", subAreaID)
                .list( row -> {
                    Optional<ServiceEntity> op = Optional.ofNullable(ServiceRepository.toService(row));
                    op.ifPresent(result::add);
                    return null;
                });
        return result;
    }


    public void addServiceToSubArea(UUID subAreaId, UUID serviceId) {
        subAreaServiceTable.insert()
                .setField("sub_area_id", subAreaId)
                .setField("service_id", serviceId)
                .execute();
    }


    public void setServicesOnSubArea(UUID subAreaId, List<UUID> services) {
        subAreaServiceTable.where("sub_area_id", subAreaId).executeDelete();

        for(int i = 0; i < services.size(); i++){
            subAreaServiceTable.insert()
                    .setField("sub_area_id", subAreaId)
                    .setField("service_id", services.get(i))
                    .execute();
        }

    }


    public void removeServiceFromAllSubAreas(UUID serviceId){
        subAreaServiceTable.where("service_id", serviceId)
                .executeDelete();

    }
    public void removeServiceFromSubArea(UUID subAreaId, UUID serviceId) {
        subAreaServiceTable.where("sub_area_id", subAreaId)
                .where("service_id", serviceId)
                .executeDelete();

    }
    //TODO bør denne være optional?
    public Map.Entry<SubAreaEntity, List<ServiceEntity>> retrieveOne(UUID sub_area_id) {
        DbContextTableAlias subAreaAlias = subAreaTable.alias("sub_area");
        DbContextTableAlias sa2s = subAreaServiceTable.alias("sa2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<SubAreaEntity, List<ServiceEntity>> result = new HashMap<>();
        subAreaAlias
                .where("id" , sub_area_id)
                .leftJoin(subAreaAlias.column("id"), sa2s.column("sub_area_id"))
                .leftJoin(sa2s.column("service_id"), service.column("id"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toSubArea(row.table(subAreaAlias)), ignored -> new ArrayList<>());

                    DatabaseRow serviceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serviceRow)));
                    return null;
                });
        return result
                .entrySet()
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: Area with id " + sub_area_id));
    }

    public List<SubAreaEntity> retriveAllShallow(){
        return subAreaTable.orderedBy("name").stream(SubAreaRepository::toSubArea).collect(Collectors.toList());
    }

    //TODO bør denne være optional?
    public Map<SubAreaEntity, List<ServiceEntity>> retrieveAll() {
        DbContextTableAlias subAreaAlias = subAreaTable.alias("sub_area");
        DbContextTableAlias sa2s = subAreaServiceTable.alias("sa2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<SubAreaEntity, List<ServiceEntity>> result = new HashMap<>();
        subAreaAlias
                .leftJoin(subAreaAlias.column("id"), sa2s.column("sub_area_id"))
                .leftJoin(sa2s.column("service_id"), service.column("id"))
                .orderBy(subAreaAlias.column("name"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toSubArea(row.table(subAreaAlias)), ignored -> new ArrayList<>());

                    DatabaseRow serviceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serviceRow)));
                    return null;
                });

        return result;
    }

    static SubAreaEntity toSubArea(DatabaseRow row){
        try {
            return new SubAreaEntity()
                    .setId(row.getUUID("id"))
                    .setName(row.getString("name"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    public Query query() {
        return new Query(areaTable.query());
    }

    public List<SubAreaEntity> getAreasContainingService(UUID service_id) {
        DbContextTableAlias subAreaAlias = subAreaTable.alias("sub_area");
        DbContextTableAlias sa2s = subAreaServiceTable.alias("sa2s");
        return subAreaAlias
                .leftJoin(subAreaAlias.column("id"), sa2s.column("sub_area_id"))
                .orderBy(subAreaAlias.column("name"))
                .where("sa2s.service_id",service_id)
                .stream(SubAreaRepository::toSubArea).collect(Collectors.toList());
    }

    public static class Query {

        private final DbContextSelectBuilder query;

        public Query(DbContextSelectBuilder query) {
            this.query = query;
        }

        public Stream<SubAreaEntity> stream() {
            return query.stream(SubAreaRepository::toSubArea);
        }

        private Query query(DbContextSelectBuilder query) {
            return this;
        }
    }
}
