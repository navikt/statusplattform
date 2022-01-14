package no.nav.portal.rest.api;

import nav.portal.core.entities.*;
import nav.portal.core.enums.ServiceType;
import no.portal.web.generated.api.*;

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
        entity.setPolling_url(dto.getPollingUrl());
        return entity;
    }

    public static AreaEntity toAreaEntity(AreaDto dto){
        AreaEntity entity = new AreaEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIcon(dto.getIcon());
        return entity;
    }


    public static ServiceDto toServiceDtoShallow(ServiceEntity entity){
        ServiceDto dto = new ServiceDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(ServiceTypeDto.fromValue(entity.getType().getDbRepresentation()));
        dto.setTeam(entity.getTeam());
        dto.setMonitorlink(entity.getMonitorlink());
        return dto;
    }

    public static ServiceDto toServiceDtoDeep(ServiceEntity service ,List<ServiceEntity> dependencies){
        Map<ServiceEntity, List<ServiceEntity>> map = new HashMap<>();
        map.put(service,dependencies);
        return toServiceDtoDeep(map.entrySet().stream().findFirst().orElseThrow());
    }

    public static ServiceDto toServiceDtoDeep(Map.Entry<ServiceEntity,List<ServiceEntity>> entry){
        ServiceEntity service = entry.getKey();
        List<ServiceEntity> dependencies = entry.getValue();
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setType(ServiceTypeDto.fromValue(service.getType().getDbRepresentation()));
        dto.setTeam(service.getTeam());
        dto.setMonitorlink(service.getMonitorlink());
        dto.pollingUrl(service.getPolling_url());
        dto.setDependencies(dependencies.stream().map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList()));
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

    public static List<ServiceStatusDto> toServiceStatusDto(List<RecordEntity> recordHistory) {
        return recordHistory.stream().map(EntityDtoMappers::serviceStatusDto).collect(Collectors.toList());
    }
    public static ServiceStatusDto serviceStatusDto(RecordEntity recordEntity){
        ServiceStatusDto dto = new ServiceStatusDto();
        dto.serviceId(recordEntity.getServiceId());
        dto.setStatus(StatusDto.fromValue(recordEntity.getStatus().getDbRepresentation()));
        return dto;
    }
}
