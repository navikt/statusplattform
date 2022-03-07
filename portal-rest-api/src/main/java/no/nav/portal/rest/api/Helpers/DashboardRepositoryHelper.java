package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.SubAreaRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.fluentjdbc.DbContext;


import java.util.UUID;
import java.util.stream.Collectors;

public class DashboardRepositoryHelper {

    private final DashboardRepository dashboardRepository;
    private final RecordRepository recordRepository;
    private final AreaRepository areaRepository;
    private final SubAreaRepository subAreaRepository;


    public DashboardRepositoryHelper(DbContext dbContext) {
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
        this.areaRepository = new AreaRepository(dbContext);
        this.subAreaRepository = new SubAreaRepository(dbContext);
    }

    public DashboardDto getDashboard(UUID dashboard_id){
        DashboardDto dashboardDto = EntityDtoMappers.toDashboardDtoDeep(dashboardRepository.retrieveOne(dashboard_id));
        dashboardDto.getAreas().forEach(areaDto -> {
            areaDto.setSubAreas(areaRepository.getSubAreasOnArea(areaDto.getId())
                            .stream()
                            .map(subAreaEntity -> EntityDtoMappers.toSubAreaDtoDeep(subAreaEntity, subAreaRepository.getServisesOnSubArea(subAreaEntity.getId())))
                            .collect(Collectors.toList()));
                });
        dashboardDto.getAreas()
                .forEach(area -> area.getServices()
                        .forEach(this::settStatusOnService));
        dashboardDto.getAreas()
                .forEach(area -> area.getSubAreas().forEach(subArea -> subArea.getServices()
                        .forEach(this::settStatusOnService)));
        dashboardDto.getAreas().forEach(areaDto -> areaDto.setStatus(getAreaStatus(areaDto)));
        return dashboardDto;
    }

    private void settStatusOnService(ServiceDto service){
        if(recordRepository.getLatestRecord(service.getId()).isPresent()){
            service.setStatus(StatusDto.fromValue(recordRepository.getLatestRecord(service.getId()).get().getStatus().getDbRepresentation().toUpperCase()));
            return;
        }
        service.setStatus(StatusDto.ISSUE);
    }

    private StatusDto getAreaStatus(AreaDto dto){
        if(dto.getServices()
                .stream()
                .map(s -> s.getStatus())
                .collect(Collectors.toList())
                .contains(StatusDto.DOWN)) return StatusDto.DOWN;
        if(dto.getServices()
                .stream()
                .map(s -> s.getStatus())
                .collect(Collectors.toList())
                .contains(StatusDto.ISSUE)) return StatusDto.ISSUE;
        return StatusDto.OK;
    }


    public void deleteDashboard(UUID dashboard_id) {
        dashboardRepository.deleteAreasFromDashboard(dashboard_id);
        dashboardRepository.deleteDashboard(dashboard_id);

    }
}
