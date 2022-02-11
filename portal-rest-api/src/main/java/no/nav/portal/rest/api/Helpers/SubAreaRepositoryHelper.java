package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.entities.SubAreaEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.SubAreaDto;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SubAreaRepositoryHelper {
    private final AreaRepository areaRepository;
    private final SubAreaRepository subAreaRepository;
    private final DashboardRepository dashboardRepository;
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;


    public SubAreaRepositoryHelper(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.subAreaRepository = new SubAreaRepository(dbContext);
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }




    public SubAreaDto newSubArea(SubAreaDto subAreaDto){
        UUID uuid = subAreaRepository.save(EntityDtoMappers.toSubAreaEntity(subAreaDto));
        Map.Entry<SubAreaEntity, List<ServiceEntity>> subArea = subAreaRepository.retrieveOne(uuid);
        return EntityDtoMappers.toSubAreaDtoDeep(subArea.getKey(), subArea.getValue());
    }

    public SubAreaDto updateArea(UUID subAreaId, SubAreaDto subAreaDto){
        subAreaDto.setId((subAreaId));
        subAreaRepository.updateSubArea(EntityDtoMappers.toSubAreaEntity(subAreaDto));
        Map.Entry<SubAreaEntity, List<ServiceEntity>> subArea = subAreaRepository.retrieveOne(subAreaId);
        return EntityDtoMappers.toSubAreaDtoDeep(subArea.getKey(), subArea.getValue());
    }


    public List<AreaDto> getAreasOnDashboard(UUID dashboardName_id){
        return dashboardRepository.retrieveOne(dashboardName_id)
                .getValue()
                .stream()
                .map(as -> EntityDtoMappers
                        .toAreaDtoDeep(as.getArea(),as.getServices()))
                .collect(Collectors.toList());
    }


    public List<AreaDto> getAreasOnDashboard(String dashboardName){
        UUID dashboardUid = dashboardRepository.uidFromName(dashboardName);
        return getAreasOnDashboard(dashboardUid);
    }





}
