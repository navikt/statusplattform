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
    public static List<AreaDto> toAreaDtoDeep(Map<AreaEntity,List<ServiceEntity>> areasWithservices){
        List<AreaDto> dtos = new ArrayList<>();
        areasWithservices.forEach((area, services) -> dtos.add(toAreaDtoDeep(area, services)));
        return dtos;
    }

    public static AreaDto toAreaDtoDeep(AreaEntity area, List<ServiceEntity> services){
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
    public static List<AreaDto> toAreaDtoShallow(List<AreaEntity> entities){
        return entities.stream().map(EntityDtoMappers::toAreaDtoShallow).collect(Collectors.toList());
    }
    public static AreaDto toAreaDtoShallow(AreaEntity area){
        AreaDto dto = new AreaDto();
        dto.setId(area.getId());
        dto.setName(area.getName());
        dto.setBeskrivelse(area.getDescription());
        dto.setIkon(area.getIcon());

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

    public static DashboardDto toDashboardDtoShallow(DashboardEntity entity){
        DashboardDto dto = new DashboardDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    public static List<DashboardDto>toDashboardDtoShallow(List<DashboardEntity> entities){
        return entities.stream().map(EntityDtoMappers::toDashboardDtoShallow).collect(Collectors.toList());
    }

}
