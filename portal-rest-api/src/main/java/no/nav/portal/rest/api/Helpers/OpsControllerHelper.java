
package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.OpsMessageEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class OpsControllerHelper {
    private final OpsRepository opsRepository;

    public OpsControllerHelper(DbContext dbContext) {
        this.opsRepository = new OpsRepository(dbContext);
    }




    public OPSmessageDto newOps(OPSmessageDto opsMessageDto){
        UUID uuid = opsRepository.save(EntityDtoMappers.toOpsMessageEntity(opsMessageDto), opsMessageDto.getAffectedServices().stream().map(ServiceDto::getId).toList());
        Map.Entry<OpsMessageEntity, List<ServiceEntity>> ops = opsRepository.retrieveOne(uuid);
        return EntityDtoMappers.toOpsMessageDtoDeep(ops.getKey(), ops.getValue());
    }

    public OPSmessageDto getOpsMessage(UUID ops_id) {
        Map.Entry<OpsMessageEntity, List<ServiceEntity>> retrievedOpsMessageEntity = opsRepository.retrieveOne(ops_id);
        return EntityDtoMappers.toOpsMessageDtoDeep(retrievedOpsMessageEntity.getKey(),
                retrievedOpsMessageEntity.getValue());
    }

    public List<OPSmessageDto> getAllOpsMessages() {
        Map<OpsMessageEntity,List<ServiceEntity>> retrievedOpsMessageEntities = opsRepository.retrieveAll();
        List<OPSmessageDto> result = new ArrayList<>();
        retrievedOpsMessageEntities.forEach((k,v)-> result.add(EntityDtoMappers.toOpsMessageDtoDeep(k,v)));
        return result;
    }

    public List<OPSmessageDto> getOpsMessagesForDashboard(DashboardDto dashboardDto){
        ArrayList<UUID> servicesOnDashboard = new ArrayList<>();

        dashboardDto.getAreas().forEach(area ->
            area.getServices().stream().map(ServiceDto::getId).forEach(serviceId -> {
                    if(!servicesOnDashboard.contains(serviceId)){
                        servicesOnDashboard.add(serviceId);
                    }
            }
            )
        );
        return  opsRepository.retrieveAllForServices(servicesOnDashboard)
                .stream().map(EntityDtoMappers::toOpsMessageDtoShallow)
                .collect(Collectors.toList());

    }


    public OPSmessageDto updateOpsMessage(OPSmessageDto opsMessageDto){
        opsRepository.setServicesOnOpsMessage(opsMessageDto.getId(), opsMessageDto.getAffectedServices().stream()
                .map(ServiceDto::getId).collect(Collectors.toList()));

        opsRepository.updateOpsMessage(EntityDtoMappers.toOpsMessageEntity(opsMessageDto));
        Map.Entry<OpsMessageEntity, List<ServiceEntity>> opsMessage = opsRepository.retrieveOne(opsMessageDto.getId());

        return EntityDtoMappers.toOpsMessageDtoDeep(opsMessage.getKey(), opsMessage.getValue());
    }

    public void deleteOps(UUID id) {
        opsRepository.deleteOps(id);
    }


    public List<OPSmessageDto> getAllForDashboard(UUID dashboard_id) {
        List<OpsMessageEntity> opsMessageEntities = opsRepository.getAllForDashboard(dashboard_id);
        return opsMessageEntities.stream()
                .map(EntityDtoMappers::toOpsMessageDtoShallow)
                .collect(Collectors.toList());
    }
}
