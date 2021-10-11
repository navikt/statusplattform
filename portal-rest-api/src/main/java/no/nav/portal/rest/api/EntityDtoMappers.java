package no.nav.portal.rest.api;

import nav.portal.core.entities.*;
import nav.portal.core.enums.ServiceType;
import no.portal.web.generated.api.*;

import java.util.*;
import java.util.stream.Collectors;


public class EntityDtoMappers {

    public static ServiceEntity toServiceEntity(ServiceDto dto){
        ServiceEntity entity = new ServiceEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(ServiceType.fromDb(dto.getType().getValue()));
        entity.setTeam(dto.getTeam());
        entity.setMonitorlink(dto.getMonitorlink());
        entity.setDescription(dto.getDescription());
        entity.setLogglink(dto.getLogglink());
        return entity;
    }
    public static AreaEntity toAreaEntity(AreaDto dto){
        AreaEntity entity = new AreaEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getBeskrivelse());
        entity.setIcon(dto.getIkon());
        return entity;
    }


    public static ServiceDto toServiceDto(ServiceEntity entity){
        ServiceDto dto = new ServiceDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(ServiceTypeDto.fromValue(entity.getType().getDbRepresentation()));
        dto.setTeam(entity.getTeam());
        dto.setMonitorlink(entity.getMonitorlink());
        dto.setDescription(entity.getDescription());
        dto.setLogglink(entity.getLogglink());
        return dto;
    }

    public static AreaDto toAreaDto(AreaEntity area, List<ServiceEntity> services){
        AreaDto dto = new AreaDto();
        dto.setId(area.getId());
        dto.setName(area.getName());
        dto.setBeskrivelse(area.getDescription());
        dto.setIkon(area.getIcon());
        dto.setServises(
                services.stream()
                        .map(EntityDtoMappers::toServiceDto)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    public static DashboardDto toDashboardDto(DashboardEntity entity,List<AreaWithServices> areas) {
        DashboardDto dto = new DashboardDto();
        dto.setName(entity.getName());
        dto.setAreas(areas
                .stream()
                .map(area -> toAreaDto(area.getArea(), area.getServices()))
                .collect(Collectors.toList()));
        return dto;
    }
    public static List<DashboardDto> toDashboardDto(Map<DashboardEntity,List<AreaWithServices>> dashboardMap){
        List<DashboardDto> result = Collections.EMPTY_LIST;
        dashboardMap.keySet().forEach(dashboard ->
                result.add(toDashboardDto(dashboard,dashboardMap.get(dashboard))));
        return result;
    }

}
