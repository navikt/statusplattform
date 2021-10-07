package no.nav.portal.rest.api;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.DashboardEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.DashboardDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.ServiceTypeDto;

import java.util.*;
import java.util.stream.Collectors;


public class EntityDtoMappers {

    public static AreaEntity toEntity(AreaDto dto){
        AreaEntity entity = new AreaEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setBeskrivelse(dto.getBeskrivelse());
        entity.setIkon(dto.getIkon());
        return entity;
    }

    public static AreaDto toDto(AreaEntity entity){
        AreaDto dto = new AreaDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBeskrivelse(entity.getBeskrivelse());
        dto.setIkon(entity.getIkon());
        return dto;
    }
    public static ServiceDto toDto(ServiceEntity entity){
        ServiceDto dto = new ServiceDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(toDto(entity.getType()));//TODO DENNE SKAL VÆRE ENUM
        dto.setTeam(entity.getTeam());
        dto.setDependencies(entity.getDependencies()
                .stream()
                .map(EntityDtoMappers::toDto)
                .collect(Collectors.toList()));
        dto.setMonitorlink(entity.getMonitorlink());
        dto.setDescription(entity.getDescription());
        dto.setLogglink(entity.getLogglink());
        return dto;
    }
    public static DashboardDto toDto(DashboardEntity entity){
        DashboardDto dto = new DashboardDto();
        dto.setName(entity.getName());
        dto.setAreas(entity.getAreas()
                .stream()
                .map(EntityDtoMappers::toDto)
                .collect(Collectors.toList()));
        return dto;
    }
    public static ServiceTypeDto toDto(ServiceType serviceType){
        return ServiceTypeDto.fromValue(serviceType.getDbRepresentation());
    }
}
