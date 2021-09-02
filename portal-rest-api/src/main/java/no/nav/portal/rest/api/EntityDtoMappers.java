package no.nav.portal.rest.api;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.DashboardEntity;
import nav.portal.core.entities.ServiceEntity;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.DashboardDto;
import no.portal.web.generated.api.ServiceDto;

public class EntityDtoMappers {

    public static AreaEntity toEntity(AreaDto dto){
        AreaEntity entity = new AreaEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setBeskrivelse(dto.getBeskrivelse());
        entity.setIkon(dto.getIkon());
        entity.setRangering(dto.getRangering());
        return entity;
    }

    public static AreaDto toDto(AreaEntity entity){
        AreaDto dto = new AreaDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBeskrivelse(entity.getBeskrivelse());
        dto.setIkon(entity.getIkon());
        dto.setRangering(entity.getRangering());
        dto.setServisesIds(entity.getServisesIds());
        return dto;
    }
    public static ServiceDto toDto(ServiceEntity entity){
        ServiceDto dto = new ServiceDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setTeam(entity.getTeam());
        dto.setDependencies(entity.getDependencies());
        dto.setMonitorlink(entity.getMonitorlink());
        dto.setDescription(entity.getDescription());
        dto.setLogglink(entity.getLogglink());
        return dto;
    }
    public static DashboardDto toDto(DashboardEntity entity){
        DashboardDto dto = new DashboardDto();
        dto.setName(entity.getName());
        dto.setAreasIds(entity.getAreasIds());
        return dto;
    }
}
