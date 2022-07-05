package nav.portal.core.repositories;

import nav.portal.core.entities.*;
import nav.portal.core.enums.ServiceType;
import nav.portal.core.exceptionHandling.ExceptionUtil;
import org.actioncontroller.HttpRequestException;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceRepository {

    private final DbContextTable serviceTable;
    private final DbContextTable service_serviceTable;
    private final DbContextTable service_maintenanceTable;
    private final DbContextTable serviceHistoryTable;
    private final DbContextTable service_openingHoursTable;


    public ServiceRepository(DbContext dbContext) {
        serviceHistoryTable = dbContext.table("daily_status_aggregation_service");
        serviceTable = dbContext.table("service");
        service_serviceTable = dbContext.table("service_service");
        service_maintenanceTable = dbContext.table("service_maintenance");
        service_openingHoursTable = dbContext.table("service_opening_hours");
    }


    public void saveOpeningHours(List<OpeningHoursEntity> openingHoursEntities){
            openingHoursEntities.forEach(this::saveOpeningHours);
    }

    public void saveOpeningHours(OpeningHoursEntity openingHoursEntity){
        service_openingHoursTable.newSaveBuilderWithUUID("id", openingHoursEntity.getId())
                .setField("service_id", openingHoursEntity.getService_id())
                .setField("day_of_the_week", openingHoursEntity.getDay_of_the_week())
                .setField("opening_time", openingHoursEntity.getOpening_time())
                .setField("closing_time", openingHoursEntity.getClosing_time())
                .execute()
                .getId();

    }


    public UUID save(ServiceEntity service) {
        //Sjekk på navn+type kombinasjon
        if(serviceTable.where("name",service.getName())
                .where("deleted",false)
                .where("type", service.getType()).getCount()>0){
            throw new HttpRequestException("Tjeneste med navn: "+ service.getName()
                    +", og type: "+service.getType()+" finnes allerede");
        }
        return serviceTable.newSaveBuilderWithUUID("id", service.getId())
                .setField("name", service.getName())
                .setField("type", service.getType().getDbRepresentation())
                .setField("team", service.getTeam())
                .setField("monitorlink", service.getMonitorlink())
                .setField("polling_url", service.getPolling_url())
                .setField("status_not_from_team", service.getStatusNotFromTeam())
                .execute()
                .getId();
    }
    public UUID saveMaintenance(MaintenanceEntity maintenanceEntity) {
        return service_maintenanceTable.newSaveBuilderWithUUID("id", maintenanceEntity.getId())
                .setField("service_id", maintenanceEntity.getServiceId())
                .setField("description", maintenanceEntity.getDescription())
                .setField("start_time", maintenanceEntity.getStart_time())
                .setField("end_time", maintenanceEntity.getEnd_time())
                .execute()
                .getId();
    }

    public List<MaintenanceEntity> getMaintenanceForService(UUID serviceId){
        return service_maintenanceTable.where("service_id", serviceId)
                .list(ServiceRepository::toMaintenanceEntity);
    }



    public void update(ServiceEntity service){
        serviceTable.where("id",service.getId())
                .update()
                .setField("name", service.getName())
                .setField("type", service.getType().getDbRepresentation())
                .setField("team", service.getTeam())
                .setField("monitorlink", service.getMonitorlink())
                .setField("polling_url", service.getPolling_url()== null? null:service.getPolling_url())
                .setField("status_not_from_team", service.getStatusNotFromTeam())
                .execute();
    }

    public Optional<ServiceEntity> retrieve(UUID id) {
        return serviceTable.where("id", id)
                .singleObject(ServiceRepository::toService);
    }

    public void addDependencyToService(ServiceEntity service, List<ServiceEntity> services){
        services.forEach(dependency ->
                service_serviceTable.insert()
                        .setField("service1_id", service.getId())
                        .setField("service2_id", dependency.getId())
                        .execute()
        );
    }
    public void addDependencyToService(UUID service1_id, UUID service2_id){
                service_serviceTable.insert()
                        .setField("service1_id", service1_id)
                        .setField("service2_id", service2_id)
                        .execute();
    }
    public void removeDependencyFromService(UUID service1_id, UUID service2_id){
        service_serviceTable
                .where("service1_id", service1_id)
                .where("service2_id", service2_id)
                .executeDelete();
    }

    public void resetDependenciesOnService(UUID serviceId){
        //Sletter både avhengigheter fra tjenesten til andre tjenester, og andre tjenesters avhengighet til tjenesten
        service_serviceTable.where("service1_id", serviceId).executeDelete();
        service_serviceTable.where("service2_id", serviceId).executeDelete();
    }

    public void removeAllDependenciesFromService(UUID serviceId){
        //Sletter både avhengigheter fra tjenesten til andre tjenester
        service_serviceTable.where("service1_id", serviceId).executeDelete();
    }



    public Boolean isOtherServicesDependentOn(UUID id) {
        return service_serviceTable.where("service2_id", id).getCount() > 0;

    }

    public Map.Entry<ServiceEntity, List<ServiceEntity>> retrieveOneWithDependencies(UUID service_id) {
        DbContextTableAlias s2s = service_serviceTable.alias("s2s");
        DbContextTableAlias service = serviceTable.alias("service");
        DbContextTableAlias dependentService = serviceTable.alias("dependent_service");

        Map<ServiceEntity, List<ServiceEntity>> result = new HashMap<>();
        service.where("id", service_id)
                .where("service.deleted", Boolean.FALSE)
                .leftJoin(service.column("id"), s2s.column("service1_id"))
                .leftJoin(s2s.column("service2_id"), dependentService.column("id"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toService(row.table(service)), ignored -> new ArrayList<>());

                    DatabaseRow dependentServiceRow = row.table(dependentService);
                    Optional.ofNullable(row.getUUID("service1_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(dependentServiceRow)));
                    return null;
                });
        return result
                .entrySet()
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: Service with id " + service_id));
    }

    public  List<ServiceEntity> retrieveAllShallow() {
        return  serviceTable.where("deleted",Boolean.FALSE).list(ServiceRepository::toService);

    }

    public Map<ServiceEntity, List<ServiceEntity>> retrieveAllDeep() {
        DbContextTableAlias s2s = service_serviceTable.alias("s2s");
        DbContextTableAlias service = serviceTable.alias("service");
        DbContextTableAlias dependentService = serviceTable.alias("dependent_service");


        Map<ServiceEntity, List<ServiceEntity>> result = new HashMap<>();
        service.where("deleted",Boolean.FALSE)
                .leftJoin(service.column("id"), s2s.column("service1_id"))
                .leftJoin(s2s.column("service2_id"), dependentService.column("id"))
                .orderBy(service.column("name"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toService(row.table(service)), ignored -> new ArrayList<>());

                    DatabaseRow dependentServiceRow = row.table(dependentService);
                    Optional.ofNullable(row.getUUID("service1_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(dependentServiceRow)));
                    return null;
                });
        return result;
    }


    public Map<ServiceEntity, List<ServiceEntity>> retrieveAllComponents() {
        return retrieveAllWithType(ServiceType.KOMPONENT);
    }

    public Map<ServiceEntity, List<ServiceEntity>> retrieveAllServices() {
        return retrieveAllWithType(ServiceType.TJENESTE);
    }

    private Map<ServiceEntity, List<ServiceEntity>> retrieveAllWithType(ServiceType serviceType) {

        DbContextTableAlias s2s = service_serviceTable.alias("s2s");
        DbContextTableAlias service = serviceTable.alias("service");
        DbContextTableAlias dependentService = serviceTable.alias("dependent_service");


        Map<ServiceEntity, List<ServiceEntity>> result = new HashMap<>();
        service.where("deleted",Boolean.FALSE)
                .where("service.type", serviceType.getDbRepresentation())
                .leftJoin(service.column("id"), s2s.column("service1_id"))
                .leftJoin(s2s.column("service2_id"), dependentService.column("id"))
                .orderBy(service.column("name"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toService(row.table(service)), ignored -> new ArrayList<>());

                    DatabaseRow dependentServiceRow = row.table(dependentService);
                    Optional.ofNullable(row.getUUID("service1_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(dependentServiceRow)));
                    return null;
                });
        return result;
    }


    public List<ServiceEntity> getServicesDependantOnComponent(UUID component_id) {
        DbContextTableAlias serviceAlias = serviceTable.alias("service");
        DbContextTableAlias s2k = service_serviceTable.alias("s2k");
        return  serviceAlias
                .leftJoin(serviceAlias.column("id"), s2k.column("service1_id"))
                .orderBy(serviceAlias.column("name"))
                .where("s2k.service2_id",component_id)
                .stream(ServiceRepository::toService).collect(Collectors.toList());
    }

    public List<ServiceEntity> retrieveServicesWithPolling() {
        return serviceTable.query().whereExpression("polling_url is not null").stream(ServiceRepository::toService).collect(Collectors.toList());
    }

    public Boolean doesEntryExist(UUID id){
        return serviceTable.where("id", id)
                .singleObject(ServiceRepository::toService).isPresent();
    }
    public List<ServiceEntity> retrieve(List<String> ids) {
        return serviceTable.whereIn("id", ids)
                .stream(ServiceRepository::toService)
                .collect(Collectors.toList());
    }

    public void delete(UUID id) {
        serviceTable.where("id", id)
                .update()
                .setField("deleted", Boolean.TRUE)
                .execute();
    }

    static DailyStatusAggregationForServiceEntity toDailyStatusAggregationForServiceEntity(DatabaseRow row) {
        try {
            return new DailyStatusAggregationForServiceEntity()
                    .setId(row.getUUID("id"))
                    .setService_id(row.getUUID("service_id"))
                    .setAggregation_date(row.getLocalDate("aggregation_date"))
                    .setNumber_of_status_ok(row.getInt("number_of_status_ok"))
                    .setNumber_of_status_issue(row.getInt("number_of_status_issue"))
                    .setNumber_of_status_down(row.getInt("number_of_status_down"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }

    }

    static ServiceEntity toService(DatabaseRow row) {
        try {
            return new ServiceEntity()
                    .setId(row.getUUID("id"))
                    .setName(row.getString("name"))
                    .setTeam(row.getString("team"))
                    .setType(ServiceType.fromDb(row.getString("type")))
                    .setMonitorlink(row.getString("monitorlink"))
                    .setPolling_url(row.getString("polling_url"))
                    .setDeleted(row.getBoolean("deleted"))
                    .setStatusNotFromTeam(row.getBoolean("status_not_from_team"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }

    }

    static MaintenanceEntity toMaintenanceEntity(DatabaseRow row) {
        try {
            return new MaintenanceEntity()
                    .setId(row.getUUID("id"))
                    .setServiceId(row.getUUID("service_id"))
                    .setDescription(row.getString("description"))
                    .setStart_time(row.getZonedDateTime("start_time"))
                    .setEnd_time(row.getZonedDateTime("end_time"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }

    }

    public Query query() {
        return new Query(serviceTable.query());
    }

    public static class Query {

        private final DbContextSelectBuilder query;

        public Query(DbContextSelectBuilder query) {
            this.query = query;
        }

        public Stream<ServiceEntity> stream() {
            return query.stream(ServiceRepository::toService);
        }

        private Query query(DbContextSelectBuilder query) {
            return this;
        }
    }
}
