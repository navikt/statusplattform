package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.*;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardRepository {


    private final DbContextTable dashboardTable;
    private final DbContextTable dashboardAreaTable;
    private final DbContextTable externalDashboardTable;
    private final DbContext dbContext;

    public DashboardRepository(DbContext dbContext) {
        dashboardTable = dbContext.table("dashboard");
        dashboardAreaTable = dbContext.table("dashboard_area");
        externalDashboardTable = dbContext.table("external_dashboards");
        this.dbContext = dbContext;
    }

    public UUID save(String name) {
        DatabaseSaveResult<UUID> result = dashboardTable
                .newSaveBuilderWithUUID("id", UUID.randomUUID())
                .setField("name", name)
                .execute();
        return result.getId();
    }


    public void settAreasOnDashboard(UUID dashboardId, UUID areaId) {
        settAreasOnDashboard(dashboardId,List.of(areaId));

    }

    public void settAreasOnDashboard(UUID dashboardId, List<UUID> areas) {
        dashboardAreaTable.where("dashboard_id", dashboardId).executeDelete();

        for(int i = 0; i < areas.size(); i++){
            dashboardAreaTable.insert()
                    .setField("dashboard_id", dashboardId)
                    .setField("area_id", areas.get(i))
                    .setField("order_in_dashboard",i)
                    .execute();
        }

    }

    public void updateNameOfDashboard(UUID dashboardId, String name) {
        dashboardTable.where("id", dashboardId).update().setField("name", name).execute();
    }

    public List<DashboardEntity> getAllDashboardUUIDsAndNames(){
        return dashboardTable.orderedBy("name").stream(DashboardRepository::toDashboard).collect(Collectors.toList());
    }


    public UUID uidFromName(String name) {
        return dashboardTable.where("name", name)
                .singleObject(DashboardRepository::toDashboard)
                .map(dashboard -> dashboard.getId())
                .orElseThrow(() -> new IllegalArgumentException("Not found: Dashboard with name " + name));
    }

    public Map.Entry<DashboardEntity, List<AreaWithServices>> retrieveOne(UUID id) {
        DbContextTableAlias d = dashboardTable.alias("d");
        DbContextTableAlias d2a = dashboardAreaTable.alias("d2a");

        Map<AreaEntity, List<ServiceEntity>> areaEntityListMap = new AreaRepository(dbContext).retrieveAll();
        Map<UUID, AreaEntity> areaById = areaEntityListMap.keySet().stream()
                .collect(Collectors.toMap(AreaEntity::getId, areaEntity -> areaEntity));

        Map<DashboardEntity, List<AreaWithServices>> result = new HashMap<>();

        d.where("id",id)
                .leftJoin(d.column("id"), d2a.column("dashboard_id"))
                .orderBy(d2a.column("order_in_dashboard"))
                .list(row -> {
                    List<AreaWithServices> areasInDashboard = result.computeIfAbsent(toDashboard(row.table(d)), ignored -> new ArrayList<>());
                    Optional.ofNullable(row.getUUID("area_id"))
                            .map(areaById::get)
                            .ifPresent(area -> areasInDashboard.add(new AreaWithServices(area, areaEntityListMap.get(area),Collections.emptyList())));
                    return null;
                });
        result.entrySet().stream().findFirst().get().getValue().forEach(this::settSubAreasOnArea);
        return result.entrySet().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: Dashboard with UUID " + id));
    }

    private void settSubAreasOnArea(AreaWithServices areaWithServices) {
        UUID areaID = areaWithServices.getArea().getId();
        List<SubAreaWithServices> subAreasOnArea = new ArrayList<>();

        DbContextTableAlias sa = dbContext.table("sub_area").alias("sa");
        DbContextTableAlias a2sa = dbContext.table("area_sub_area").alias("a2sa");


        a2sa.where("area_id", areaID)
                .leftJoin(a2sa.column("sub_area_id"),sa.column("id"))
                .list(row -> subAreasOnArea.add( new SubAreaWithServices(SubAreaRepository.toSubArea(row.table(sa)),null )));

        SubAreaRepository subAreaRepository = new SubAreaRepository(dbContext);



        subAreasOnArea.forEach(subAreaWithServices ->
                subAreaWithServices.setServices(
                        subAreaRepository.getServicesOnSubArea(subAreaWithServices.getSubArea().getId()))
        );
        areaWithServices.setSubAreas(subAreasOnArea);
    }

    public Map.Entry<DashboardEntity, List<AreaWithServices>> retrieveOneFromName(String name) {
        DbContextTableAlias d = dashboardTable.alias("d");
        DbContextTableAlias d2a = dashboardAreaTable.alias("d2a");

        Map<AreaEntity, List<ServiceEntity>> areaEntityListMap = new AreaRepository(dbContext).retrieveAll();
        Map<UUID, AreaEntity> areaById = areaEntityListMap.keySet().stream()
                .collect(Collectors.toMap(AreaEntity::getId, areaEntity -> areaEntity));

        Map<DashboardEntity, List<AreaWithServices>> result = new HashMap<>();

        d.where("name",name)
                .leftJoin(d.column("id"), d2a.column("dashboard_id"))
                .orderBy(d2a.column("order_in_dashboard"))
                .list(row -> {
                    List<AreaWithServices> areasInDashboard = result.computeIfAbsent(toDashboard(row.table(d)), ignored -> new ArrayList<>());
                    Optional.ofNullable(row.getUUID("area_id"))
                            .map(areaById::get)
                            .ifPresent(area -> areasInDashboard.add(new AreaWithServices(area, areaEntityListMap.get(area),Collections.EMPTY_LIST)));
                    return null;
                });
        return result.entrySet().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: Dashboard with name " + name));
    }


    public Map<DashboardEntity, List<AreaWithServices>> retrieveAll() {
        DbContextTableAlias d = dashboardTable.alias("d");
        DbContextTableAlias d2a = dashboardAreaTable.alias("d2a");

        Map<AreaEntity, List<ServiceEntity>> areaEntityListMap = new AreaRepository(dbContext).retrieveAll();
        Map<UUID, AreaEntity> areaById = areaEntityListMap.keySet().stream()
                .collect(Collectors.toMap(AreaEntity::getId, areaEntity -> areaEntity));

        Map<DashboardEntity, List<AreaWithServices>> result = new HashMap<>();

        d.leftJoin(d.column("id"), d2a.column("dashboard_id"))
                .orderBy(d2a.column("order_in_dashboard"))
                .list(row -> {
                    List<AreaWithServices> areasInDashboard = result.computeIfAbsent(toDashboard(row.table(d)), ignored -> new ArrayList<>());
                    Optional.ofNullable(row.getUUID("area_id"))
                            .map(areaById::get)
                            .ifPresent(area -> areasInDashboard.add(new AreaWithServices(area, areaEntityListMap.get(area),Collections.EMPTY_LIST)));
                    return null;
                });
        return result;
    }

    private static DashboardEntity toDashboard(DatabaseRow row) {
        try {
            return new DashboardEntity()
                    .setId(row.getUUID("id"))
                    .setName(row.getString("name"));
        } catch (SQLException e) {
           throw ExceptionUtil.soften(e);
        }
    }

    public void deleteAreasFromDashboard(UUID id) {
        dashboardAreaTable.where("dashboard_id", id).executeDelete();
    }

    public void deleteDashboard(UUID id) {
        dashboardTable.where("id", id).executeDelete();
    }


    // External Dashboards CRUD

    public UUID saveExternalDashboard(UUID dashboardId) {
        DatabaseSaveResult<UUID> result = externalDashboardTable
                .newSaveBuilderWithUUID("id", UUID.randomUUID())
                .setField("dashboard_id", dashboardId)
                .execute();
        return result.getId();
    }

    private DashboardEntity getDashboardEntityById(UUID id) {
        return dashboardTable.where("id", id)
                .singleObject(DashboardRepository::toDashboard)
                .orElseThrow(() -> new IllegalArgumentException("Not found: Dashboard with ID " + id));
    }

    public List<Map.Entry<DashboardEntity, List<AreaWithServices>>> getAllExternalDashboards() {
        // Fetch all external dashboard IDs
        List<UUID> externalDashboardIds = externalDashboardTable
                .query()
                .list(row -> row.getUUID("dashboard_id"));

        // For each external dashboard ID, fetch its associated data using the existing retrieveOne method
        return externalDashboardIds.stream()
                .map(this::retrieveOne)
                .collect(Collectors.toList());
    }

    public void updateExternalDashboard(UUID externalDashboardId, UUID newDashboardId) {
        externalDashboardTable.where("id", externalDashboardId)
                .update()
                .setField("dashboard_id", newDashboardId)
                .execute();
    }

    public void deleteExternalDashboard(UUID externalDashboardId) {
        externalDashboardTable.where("id", externalDashboardId)
                .executeDelete();
    }

    public Map<OpsMessageEntity, List<ServiceEntity>> getOpsMessagesByDashboardId(UUID dashboardId) {
        DbContextTableAlias dashboardAlias = dashboardTable.alias("d");
        DbContextTableAlias dashboardAreaAlias = dashboardAreaTable.alias("da");
        DbContextTableAlias areaServiceAlias = dbContext.table("area_service").alias("asr");
        DbContextTableAlias serviceAlias = dbContext.table("service").alias("s");
        DbContextTableAlias opsMessageServiceAlias = dbContext.table("ops_message_service").alias("oms");
        DbContextTableAlias opsMessageAlias = dbContext.table("ops_message").alias("om");

        // Use a map to store each OpsMessageEntity and its associated services
        Map<OpsMessageEntity, List<ServiceEntity>> opsMessageWithServices = new HashMap<>();

        // Query and populate the result map
        dashboardAlias.where("id", dashboardId)
                .leftJoin(dashboardAlias.column("id"), dashboardAreaAlias.column("dashboard_id")) // Join dashboard and dashboard_area on dashboard_id
                .leftJoin(dashboardAreaAlias.column("area_id"), areaServiceAlias.column("area_id")) // Join dashboard_area and area_service on area_id
                .leftJoin(areaServiceAlias.column("service_id"), serviceAlias.column("id")) // Join area_service and service on service_id
                .leftJoin(serviceAlias.column("id"), opsMessageServiceAlias.column("service_id")) // Join service and ops_message_service on service_id
                .leftJoin(opsMessageServiceAlias.column("ops_message_id"), opsMessageAlias.column("id")) // Join ops_message_service and ops_message on ops_message_id
                .list(row -> {
                    // Convert row data to OpsMessageEntity if the opsMessageAlias table is not null
                    OpsMessageEntity opsMessage = Optional.ofNullable(row.table(opsMessageAlias))
                            .map(OpsRepository::toOps)
                            .orElse(null);

                    // Check if the opsMessage is null; if it is, skip this row as it doesn’t have a valid OpsMessage
                    if (opsMessage == null) {
                        return null; // Skip rows without a corresponding ops message
                    }

                    // Convert row data to ServiceEntity if the serviceAlias table is not null
                    ServiceEntity service = Optional.ofNullable(row.table(serviceAlias))
                            .map(ServiceRepository::toService)
                            .orElse(null);

                    // Add the service to the appropriate OpsMessageEntity
                    if (service != null) {
                        opsMessageWithServices.computeIfAbsent(opsMessage, k -> new ArrayList<>()).add(service);
                    }

                    return null;
                });

        return opsMessageWithServices;
    }

}
