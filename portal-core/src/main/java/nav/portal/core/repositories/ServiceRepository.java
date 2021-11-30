package nav.portal.core.repositories;

import nav.portal.core.entities.ServiceEntity;
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


    public ServiceRepository(DbContext dbContext) {
        serviceTable = dbContext.table("service");
        service_serviceTable = dbContext.table("service_service");
    }

    public UUID save(ServiceEntity service) {
        //Sjekk på navn+type kombinasjon
        if(serviceTable.where("name",service.getName())
                .where("type", service.getType()).getCount()>0){
            throw new HttpRequestException("Tjeneste med navn: "+ service.getName()
                    +", og type: "+service.getType()+" finnes allerede");
        }
        return serviceTable.newSaveBuilderWithUUID("id", service.getId())
                .setField("name", service.getName())
                .setField("type", service.getType().getDbRepresentation())
                .setField("team", service.getTeam())
                .setField("monitorlink", service.getMonitorlink())
                .setField("description", service.getDescription())
                .setField("logglink", service.getLogglink())
                .execute()
                .getId();
    }

    public void update(ServiceEntity service){
        serviceTable.where("id",service.getId())
                .update()
                .setField("name", service.getName())
                .setField("type", service.getType().getDbRepresentation())
                .setField("team", service.getTeam())
                .setField("monitorlink", service.getMonitorlink())
                .setField("description", service.getDescription())
                .setField("logglink", service.getLogglink())
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

    public Map<ServiceEntity, List<ServiceEntity>> retriveAll() {
        DbContextTableAlias s2s = service_serviceTable.alias("s2s");
        DbContextTableAlias service = serviceTable.alias("service");
        DbContextTableAlias dependentService = serviceTable.alias("dependent_service");


        Map<ServiceEntity, List<ServiceEntity>> result = new HashMap<>();
        service.leftJoin(service.column("id"), s2s.column("service1_id"))
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

    public Boolean doesEntryExist(UUID id){
        return serviceTable.where("id", id)
                .singleObject(ServiceRepository::toService).isPresent();
    }
    public List<ServiceEntity> retrieve(List<String> ids) {
        return serviceTable.whereIn("id", ids)
                .stream(ServiceRepository::toService)
                .collect(Collectors.toList());
    }

    public int delete(UUID id) {
        return serviceTable.where("id", id).executeDelete();
    }




    static ServiceEntity toService(DatabaseRow row) {
        try {
            return new ServiceEntity(row.getString("name"),
                    row.getUUID("id"),
                    ServiceType.fromDb(row.getString("type")),
                    row.getString("team"),
                    row.getString("monitorlink"),
                    row.getString("description"),
                    row.getString("logglink")
                    );
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
