package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.AreaWithServices;
import nav.statusplattform.core.entities.DashboardEntity;
import nav.statusplattform.core.entities.OpsMessageEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.OpsMessageSeverity;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;

public class OpsRepository {
    private final DashboardRepository dashboardRepository;
    private final DbContextTable opsMessageTable;
    private final DbContextTable opsMessageServiceTable;
    private final DbContextTable serviceTable;
    private final DbContextTable dashboardTable;
    private final DbContextTable dashboardAreaTable;
    private final DbContext dbContext;

    public OpsRepository(DbContext dbContext) {
        dashboardRepository = new DashboardRepository( dbContext);
        opsMessageTable = dbContext.table("ops_message");
        serviceTable = dbContext.table("service");
        opsMessageServiceTable = dbContext.table("ops_message_service");
        dashboardTable = dbContext.table("dashboard");
        dashboardAreaTable = dbContext.table("dashboard_area");
        this.dbContext = dbContext;
    }

    public UUID save(OpsMessageEntity entity, List<UUID> services) {
        DatabaseSaveResult<UUID> result = opsMessageTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("intern_header", entity.getInternalHeader())
                .setField("intern_text", entity.getInternalText())
                .setField("extern_header", entity.getExternalHeader())
                .setField("extern_text", entity.getExternalText())
                .setField("only_show_for_nav_employees", entity.getOnlyShowForNavEmployees())
                .setField("start_time", entity.getStartTime())
                .setField("end_time", entity.getEndTime())
                .setField("severity", entity.getSeverity())
                .execute();
        setServicesOnOpsMessage(result.getId(), services);
        return result.getId();
    }

    public void deleteOps(UUID id) {
        opsMessageTable.where("id", id)
                .update()
                .setField("deleted", true)
                .execute();
    }

    public void setServicesOnOpsMessage(UUID opsId, List<UUID> services) {
        opsMessageServiceTable.where("ops_message_id", opsId).executeDelete();
        if(services == null || services.isEmpty()){
            return;
        }
        for(int i = 0; i < services.size(); i++){
            opsMessageServiceTable.insert()
                    .setField("ops_message_id", opsId)
                    .setField("service_id", services.get(i))
                    .execute();
        }
    }

    public Map.Entry<OpsMessageEntity, List<ServiceEntity>> retrieveOne(UUID ops_id) {
        DbContextTableAlias ops = opsMessageTable.alias("ops");
        DbContextTableAlias o2s = opsMessageServiceTable.alias("o2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<OpsMessageEntity, List<ServiceEntity>> result = new HashMap<>();
        ops
                .where("id" , ops_id)
                .leftJoin(ops.column("id"), o2s.column("ops_message_id"))
                .leftJoin(o2s.column("service_id"), service.column("id"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toOps(row.table(ops)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        return result
                .entrySet()
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: OpsMessage with id " + ops_id));
    }

    public List<OpsMessageEntity> getAllForDashboard(UUID dashboardId){

        //TODO lag logikken under kun ved sql
        Map.Entry<DashboardEntity, List<AreaWithServices>> dashboardEntityListMap = dashboardRepository.retrieveOne(dashboardId);

        List<UUID>serviceIds = new ArrayList<>();
        dashboardEntityListMap.getValue().forEach(areaWithServices -> {
            areaWithServices.getServices().stream().map(ServiceEntity::getId).forEach(serviceId -> {
                if (!serviceIds.contains(serviceId)){
                    serviceIds.add(serviceId);
                }
            });
            areaWithServices.getSubAreas().forEach(subAreaWithServices -> {
                subAreaWithServices.getServices().forEach(service -> {
                    if(!serviceIds.contains(service.getId())){
                        serviceIds.add(service.getId());
                    }
                });
            });
        });
        ArrayList<OpsMessageEntity> result = new ArrayList<>(retrieveAllForServices(serviceIds).keySet());

        Map<OpsMessageEntity,List<ServiceEntity>> allActive = retrieveAllActive();

        allActive.forEach((k,v) ->{
            if(allActive.get(k).isEmpty()){
                result.add(k);
            }
        } );

        return  result;

    }

    public Map<OpsMessageEntity, List<ServiceEntity>> retrieveAll() {
        DbContextTableAlias ops = opsMessageTable.alias("ops");
        DbContextTableAlias o2s = opsMessageServiceTable.alias("o2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<OpsMessageEntity, List<ServiceEntity>> result = new HashMap<>();
        ops
                .where("deleted",Boolean.FALSE)
                .leftJoin(ops.column("id"), o2s.column("ops_message_id"))
                .leftJoin(o2s.column("service_id"), service.column("id"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toOps(row.table(ops)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        return result;
    }
    public Map<OpsMessageEntity, List<ServiceEntity>> retrieveAllActiveForServices(List<UUID> serviceIds) {
        DbContextTableAlias ops = opsMessageTable.alias("ops");
        DbContextTableAlias o2s = opsMessageServiceTable.alias("o2s");
        DbContextTableAlias service = serviceTable.alias("service");
        ZonedDateTime today = ZonedDateTime.now();

        Map<OpsMessageEntity, List<ServiceEntity>> result = new HashMap<>();
        ops
                .where("deleted",Boolean.FALSE)
                .whereExpression("start_time <= ?", today )
                .whereExpression("end_time >= ?", today)
                .leftJoin(ops.column("id"), o2s.column("ops_message_id"))
                .leftJoin(o2s.column("service_id"), service.column("id"))
                .whereIn("o2s.service_id", serviceIds)
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toOps(row.table(ops)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        return result;
    }

    public Map<OpsMessageEntity, List<ServiceEntity>> retrieveAllForServices(List<UUID> serviceIds) {
        DbContextTableAlias ops = opsMessageTable.alias("ops");
        DbContextTableAlias o2s = opsMessageServiceTable.alias("o2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<OpsMessageEntity, List<ServiceEntity>> result = new HashMap<>();
        ops
                .where("deleted",Boolean.FALSE)
                .leftJoin(ops.column("id"), o2s.column("ops_message_id"))
                .leftJoin(o2s.column("service_id"), service.column("id"))
                .whereIn("o2s.service_id", serviceIds)
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toOps(row.table(ops)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        return result;
    }


    public Map<OpsMessageEntity, List<ServiceEntity>> retrieveAllActive() {
        DbContextTableAlias ops = opsMessageTable.alias("ops");
        DbContextTableAlias o2s = opsMessageServiceTable.alias("o2s");
        DbContextTableAlias service = serviceTable.alias("service");
        ZonedDateTime today = ZonedDateTime.now();

        Map<OpsMessageEntity, List<ServiceEntity>> result = new HashMap<>();
        ops
                .where("deleted",Boolean.FALSE)
                .whereExpression("start_time <= ?", today )
                .whereExpression("end_time >= ?", today)
                .leftJoin(ops.column("id"), o2s.column("ops_message_id"))
                .leftJoin(o2s.column("service_id"), service.column("id"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toOps(row.table(ops)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        return result;
    }



    static OpsMessageEntity toOps(DatabaseRow row){
        try {
            return new OpsMessageEntity()
                    .setId(row.getUUID("id"))
                    .setInternalHeader(row.getString("intern_header"))
                    .setInternalText(row.getString("intern_text"))
                    .setExternalHeader(row.getString("extern_header"))
                    .setExternalText(row.getString("extern_text"))
                    .setOnlyShowForNavEmployees(row.getBoolean("only_show_for_nav_employees"))
                    .setStartTime(row.getZonedDateTime("start_time"))
                    .setEndTime(row.getZonedDateTime("end_time"))
                    .setSeverity(OpsMessageSeverity.fromDb(row.getString("severity")).orElse(null))
                    .setDeleted(row.getBoolean("deleted"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }


    public void updateOpsMessage(OpsMessageEntity opsMessageEntity) {
        opsMessageTable.where("id", opsMessageEntity.getId()).update()
                .setFieldIfPresent("intern_header", opsMessageEntity.getInternalHeader())
                .setField("intern_text", opsMessageEntity.getInternalText())
                .setField("extern_header", opsMessageEntity.getExternalHeader())
                .setField("extern_text", opsMessageEntity.getExternalText())
                .setField("only_show_for_nav_employees", opsMessageEntity.getOnlyShowForNavEmployees())
                .setField("start_time", opsMessageEntity.getStartTime())
                .setField("end_time", opsMessageEntity.getEndTime())
                .setField("severity", opsMessageEntity.getSeverity())
                .execute();
    }

    public Boolean isEntryDeleted(UUID id){
        return opsMessageTable
                .where("id", id)
                .where("deleted", true)
                .singleObject(OpsRepository::toOps).isPresent();
    }

    public List<OpsMessageEntity> findOpsMessagesByServiceIds(List<UUID> serviceIds) {
        DbContextTableAlias ops = opsMessageTable.alias("ops");
        DbContextTableAlias o2s = opsMessageServiceTable.alias("o2s");

        return ops
                .leftJoin(ops.column("id"), o2s.column("ops_message_id"))
                .whereIn(o2s.column("service_id"), serviceIds)
                .where("deleted", false)
                .list(OpsRepository::toOps);
    }
}
