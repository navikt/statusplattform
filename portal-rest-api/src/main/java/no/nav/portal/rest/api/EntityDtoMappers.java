package no.nav.portal.rest.api;

import nav.portal.core.entities.*;
import nav.portal.core.enums.OpsMessageSeverity;
import nav.portal.core.enums.ServiceType;
import no.portal.web.generated.api.*;


import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class EntityDtoMappers {

    public static StatusDto toStatusDto(RecordEntity recordEntity){
        return StatusDto.fromValue(recordEntity.getStatus().getDbRepresentation());
    }

    public static ServiceEntity toServiceEntity(ServiceDto dto){
        ServiceEntity entity = new ServiceEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(ServiceType.fromDb(dto.getType().getValue()));
        entity.setTeam(dto.getTeam());
        entity.setMonitorlink(dto.getMonitorlink());
        if(dto.getPollingUrl() != null && dto.getPollingUrl().equals("")){
            entity.setPolling_url(null);
        }
        else {
            entity.setPolling_url(dto.getPollingUrl());
        }
        entity.setStatusNotFromTeam(dto.getStatusNotFromTeam());
        return entity;
    }

    //TODO kanskje denne valideringen hører hjemme et annet sted?


    public static AreaEntity toAreaEntity(AreaDto dto){
        AreaEntity entity = new AreaEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIcon(dto.getIcon());
        return entity;
    }

    public static SubAreaEntity toSubAreaEntity(SubAreaDto dto){
        SubAreaEntity entity = new SubAreaEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        return entity;
    }


    public static ServiceDto toServiceDtoShallow(ServiceEntity entity){
        ServiceDto dto = new ServiceDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(ServiceTypeDto.fromValue(entity.getType().getDbRepresentation()));
        dto.setTeam(entity.getTeam());
        dto.setPollingUrl(entity.getPolling_url());
        dto.setMonitorlink(entity.getMonitorlink());
        dto.setStatusNotFromTeam(entity.getStatusNotFromTeam());
        return dto;
    }

    public static OPSmessageDto toOpsMessageDtoShallow(OpsMessageEntity entity) {
        OPSmessageDto dto = new OPSmessageDto();
        dto.setId(entity.getId());
        dto.setInternalHeader(entity.getInternalHeader());
        dto.setInternalMessage(entity.getInternalText());
        dto.setExternalHeader(entity.getExternalHeader());
        dto.setExternalMessage(entity.getExternalText());
        dto.setStartTime(entity.getStartTime() != null? entity.getStartTime().toOffsetDateTime():null);
        dto.setEndTime(entity.getEndTime() != null? entity.getEndTime().toOffsetDateTime():null);
        dto.setSeverity(entity.getSeverity() != null?
                OPSmessageDto.SeverityEnum.fromValue(entity.getSeverity().getDbRepresentation())
                :null);
        dto.setIsActive(entity.getIsActive());
        dto.setOnlyShowForNavEmployees(entity.getOnlyShowForNavEmployees());
        return dto;
    }

    public static OPSmessageDto toOpsMessageDtoDeep(OpsMessageEntity entity, List<ServiceEntity> services) {
        OPSmessageDto dto = toOpsMessageDtoShallow(entity);
        dto.setAffectedServices(services.stream().map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList()));
        return dto;
    }

    public static OpsMessageEntity toOpsMessageEntity(OPSmessageDto opsMessageDto) {
        OpsMessageEntity opsMessageEntity = new OpsMessageEntity();
        opsMessageEntity.setId(opsMessageDto.getId());
        opsMessageEntity.setExternalHeader(opsMessageDto.getExternalHeader());
        opsMessageEntity.setExternalText(opsMessageDto.getExternalMessage());
        opsMessageEntity.setInternalHeader(opsMessageDto.getInternalHeader());
        opsMessageEntity.setInternalText(opsMessageDto.getInternalMessage());
        opsMessageEntity.setSeverity(opsMessageDto.getSeverity() != null?
                OpsMessageSeverity.valueOf((opsMessageDto.getSeverity().getValue())):
                null);
        opsMessageEntity.setIsActive(opsMessageDto.getIsActive());
        opsMessageEntity.setOnlyShowForNavEmployees(opsMessageDto.getOnlyShowForNavEmployees());
        opsMessageEntity.setStartTime(opsMessageDto.getStartTime() != null?
                opsMessageDto.getStartTime().toZonedDateTime():
                null);
        opsMessageEntity.setEndTime(opsMessageDto.getEndTime() != null?
                opsMessageDto.getEndTime().toZonedDateTime():
                null);
        return opsMessageEntity;
    }

    public static ServiceDto toServiceDtoDeep(ServiceEntity service ,List<ServiceEntity> dependencies){
        Map<ServiceEntity, List<ServiceEntity>> map = new HashMap<>();
        map.put(service,dependencies);
        return toServiceDtoDeep(map.entrySet().stream().findFirst().orElseThrow());
    }

    public static ServiceDto toServiceDtoDeep(Map.Entry<ServiceEntity,List<ServiceEntity>> entry){
        ServiceEntity service = entry.getKey();
        List<ServiceEntity> serviceDependencies = entry.getValue().stream().filter(s -> s.getType().equals(ServiceType.TJENESTE)).collect(Collectors.toList());
        List<ServiceEntity> componentDependencies = entry.getValue().stream().filter(s -> s.getType().equals(ServiceType.KOMPONENT)).collect(Collectors.toList());
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setType(ServiceTypeDto.fromValue(service.getType().getDbRepresentation()));
        dto.setTeam(service.getTeam());
        dto.setMonitorlink(service.getMonitorlink());
        dto.pollingUrl(service.getPolling_url());
        dto.setStatusNotFromTeam(service.getStatusNotFromTeam());
        dto.serviceDependencies(serviceDependencies.stream().map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList()));
        dto.setComponentDependencies(componentDependencies.stream().map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList()));
        return dto;
    }

    public static List<AreaDto> toAreaDtoDeep(Map<AreaEntity,List<ServiceEntity>> areasWithservices){
        List<AreaDto> dtos = new ArrayList<>();
        areasWithservices.forEach((area, services) -> dtos.add(toAreaDtoDeep(area, services)));
        return dtos.stream().sorted(Comparator.comparing(AreaDto::getName)).collect(Collectors.toList());
    }

    public static AreaDto toAreaDtoDeep(AreaEntity area, List<ServiceEntity> services){
        AreaDto dto = new AreaDto();
        dto.setId(area.getId());
        dto.setName(area.getName());
        dto.setDescription(area.getDescription());
        dto.setIcon(area.getIcon());
        dto.setServices(
                services.stream()
                        .map(EntityDtoMappers::toServiceDtoShallow)
                        .collect(Collectors.toList())
        );
        return dto;
    }
    public static List<AreaDto> toAreaDtoShallow(List<AreaEntity> entities){
        return entities.stream().map(EntityDtoMappers::toAreaDtoShallow).collect(Collectors.toList());
    }
    public static AreaDto toAreaDtoShallow(AreaEntity area){
        AreaDto dto = new AreaDto();
        dto.setId(area.getId());
        dto.setName(area.getName());
        dto.setDescription(area.getDescription());
        dto.setIcon(area.getIcon());

        return dto;
    }


    public static List<SubAreaDto> toSubAreaDtoDeep(Map<SubAreaEntity, List<ServiceEntity>> subAreaWithServices){
        List<SubAreaDto> dtos = new ArrayList<>();
        subAreaWithServices.forEach((subArea, services) -> dtos.add(toSubAreaDtoDeep(subArea, services)));
        return dtos.stream().sorted(Comparator.comparing(SubAreaDto::getName)).collect(Collectors.toList());
    }

    public static SubAreaDto toSubAreaDtoDeep(SubAreaEntity subArea, List<ServiceEntity> services){
        SubAreaDto dto = new SubAreaDto();
        dto.setId(subArea.getId());
        dto.setName(subArea.getName());
        dto.setServices(
                services.stream()
                        .map(EntityDtoMappers::toServiceDtoShallow)
                        .collect(Collectors.toList())
        );
        return dto;
    }
    public static List<SubAreaDto> toSubAreaDtoShallow(List<SubAreaEntity> entities){
        return entities.stream().map(EntityDtoMappers::toSubAreaDtoShallow).collect(Collectors.toList());
    }
    public static SubAreaDto toSubAreaDtoShallow(SubAreaEntity subArea){
        SubAreaDto dto = new SubAreaDto();
        dto.setId(subArea.getId());
        dto.setName(subArea.getName());

        return dto;
    }



    public static DashboardDto toDashboardDtoDeep(Map.Entry<DashboardEntity,List<AreaWithServices>> dashboardEntry) {
        DashboardDto dto = new DashboardDto();
        dto.setName(dashboardEntry.getKey().getName());
        dto.setId(dashboardEntry.getKey().getId());
        dto.setAreas(dashboardEntry.getValue()
                .stream()
                .map(area -> toAreaDtoDeep(area.getArea(), area.getServices()))
                .collect(Collectors.toList()));
        return dto;
    }
    public static List<DashboardDto> toDashboardDtoDeep(Map<DashboardEntity,List<AreaWithServices>> dashboardMap){
        List<DashboardDto> result = Collections.EMPTY_LIST;
        dashboardMap.entrySet().forEach(dashboard ->
                result.add(toDashboardDtoDeep(dashboard)));
        return result;
    }

    public static DashboardNameIdDto toDashboardDtoShallow(DashboardEntity entity){
        DashboardNameIdDto dto = new DashboardNameIdDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    public static List<DashboardNameIdDto>toDashboardDtoShallow(List<DashboardEntity> entities){
        return entities.stream().map(EntityDtoMappers::toDashboardDtoShallow).collect(Collectors.toList());
    }

    public static List<RecordDto> toRecordDto(List<RecordEntity> recordHistory) {
        return recordHistory.stream().map(EntityDtoMappers::toRecordDto).collect(Collectors.toList());
    }
    public static RecordDto toRecordDto(RecordEntity recordEntity){
        RecordDto dto = new RecordDto();
        dto.serviceId(recordEntity.getServiceId());
        dto.setStatus(StatusDto.fromValue(recordEntity.getStatus().getDbRepresentation()));
        dto.setDescription(recordEntity.getDescription());
        dto.setLogLink(recordEntity.getLogglink());
        ZonedDateTime entityTime = recordEntity.getCreated_at();
        dto.setTimestamp(OffsetDateTime.of(entityTime.toLocalDateTime(),entityTime.getOffset()));
        return dto;
    }

    public static MaintenanceEntity toMaintenanceEntity(MaintenanceDto maintenanceDto) {
        return new MaintenanceEntity()
                .setServiceId(maintenanceDto.getServiceId())
                .setDescription(maintenanceDto.getDescription())
                .setStart_time(maintenanceDto.getStartTime().toZonedDateTime())
                .setEnd_time(maintenanceDto.getEndTime().toZonedDateTime());
    }

    public static MaintenanceDto toMaintenanceDto(MaintenanceEntity maintenanceEntity){
        return new MaintenanceDto()
                .serviceId(maintenanceEntity.getServiceId())
                .description(maintenanceEntity.getDescription())
                .startTime(maintenanceEntity.getStart_time().toOffsetDateTime())
                .endTime(maintenanceEntity.getEnd_time().toOffsetDateTime());
    }
}
