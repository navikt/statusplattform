
package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.OpsMessageEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.repositories.OpsRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.generated.api.DashboardDto;
import no.nav.statusplattform.generated.api.OPSmessageDto;
import no.nav.statusplattform.generated.api.ServiceDto;
import org.fluentjdbc.DbContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class OpsControllerHelper {

    private static final Logger logger = LoggerFactory.getLogger(OpsControllerHelper.class);

    private final OpsRepository opsRepository;
    private final NotificationQueueHelper notificationQueueHelper;

    public OpsControllerHelper(DbContext dbContext) {
        this.opsRepository = new OpsRepository(dbContext);
        this.notificationQueueHelper = new NotificationQueueHelper(dbContext);
    }




    public OPSmessageDto newOps(OPSmessageDto opsMessageDto){
        List<UUID> serviceIds = opsMessageDto.getAffectedServices().stream().map(ServiceDto::getId).toList();
        UUID uuid = opsRepository.save(EntityDtoMappers.toOpsMessageEntity(opsMessageDto), serviceIds);
        Map.Entry<OpsMessageEntity, List<ServiceEntity>> ops = opsRepository.retrieveOne(uuid);

        // Queue notifications for subscribers
        try {
            notificationQueueHelper.queueOpsMessageNotifications(ops.getKey(), serviceIds);
        } catch (Exception e) {
            logger.error("Failed to queue ops message notifications for ops {}", uuid, e);
        }

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


    public OPSmessageDto updateOpsMessage(OPSmessageDto opsMessageDto){
        List<UUID> serviceIds = opsMessageDto.getAffectedServices().stream()
                .map(ServiceDto::getId).collect(Collectors.toList());
        opsRepository.setServicesOnOpsMessage(opsMessageDto.getId(), serviceIds);

        // If status is changed to SOLVED, update endTime to now
        OpsMessageEntity entityToUpdate = EntityDtoMappers.toOpsMessageEntity(opsMessageDto);
        if (opsMessageDto.getStatus() != null && opsMessageDto.getStatus().toString().equals("SOLVED")) {
            entityToUpdate.setEndTime(java.time.ZonedDateTime.now());
        }

        opsRepository.updateOpsMessage(entityToUpdate);
        Map.Entry<OpsMessageEntity, List<ServiceEntity>> opsMessage = opsRepository.retrieveOne(opsMessageDto.getId());

        // Queue notifications for subscribers
        try {
            notificationQueueHelper.queueOpsMessageNotifications(opsMessage.getKey(), serviceIds);
        } catch (Exception e) {
            logger.error("Failed to queue ops message notifications for ops {}", opsMessageDto.getId(), e);
        }

        return EntityDtoMappers.toOpsMessageDtoDeep(opsMessage.getKey(), opsMessage.getValue());
    }

    public void deleteOps(UUID id) {
        opsRepository.deleteOps(id);
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
        //Adding opsmessages without services
        Map<OpsMessageEntity, List<ServiceEntity>> allActive = opsRepository.retrieveAllActive();
        List<OPSmessageDto> result = new ArrayList<>();
        allActive.forEach((k,v) ->{
            if(allActive.get(k).isEmpty()){
                result.add(EntityDtoMappers.toOpsMessageDtoShallow(k));
            }
        } );
        //Adding opssmessages with service on dashboard
        Map<OpsMessageEntity, List<ServiceEntity>> messagesForServices = opsRepository.retrieveAllActiveForServices(servicesOnDashboard);
        messagesForServices.forEach((opsMessage, services) ->
                result.add(EntityDtoMappers.toOpsMessageDtoDeep(opsMessage,services)));

        return  result;
    }

    public List<OPSmessageDto> getOpsMessagesByServiceIds(List<String> serviceIds) {
        // Convert list of service IDs to UUIDs
        List<UUID> serviceUUIDs = serviceIds.stream().map(UUID::fromString).collect(Collectors.toList());

        // Fetch messages WITH their affected services from the repository
        Map<OpsMessageEntity, List<ServiceEntity>> messagesWithServices = opsRepository.retrieveAllForServices(serviceUUIDs);

        // Convert to DTOs with proper affectedServices populated
        List<OPSmessageDto> result = new ArrayList<>();
        messagesWithServices.forEach((opsMessage, services) ->
                result.add(EntityDtoMappers.toOpsMessageDtoDeep(opsMessage, services)));

        return result;
    }
}
