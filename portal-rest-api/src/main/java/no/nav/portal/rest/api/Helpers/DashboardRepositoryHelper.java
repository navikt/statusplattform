package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.AreaWithServices;
import nav.portal.core.entities.RecordEntity;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.fluentjdbc.DbContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DashboardRepositoryHelper {

    private final DashboardRepository dashboardRepository;
    private final RecordRepository recordRepository;


    public DashboardRepositoryHelper(DbContext dbContext) {
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }



    public DashboardDto getDashboard(UUID dashboard_id){
        DashboardDto dashboardDto = EntityDtoMappers.toDashboardDtoDeep(dashboardRepository.retrieveOne(dashboard_id));
        dashboardDto.getAreas().forEach(areaDto -> areaDto.setStatus(getAreaStatus(areaDto)));
        return dashboardDto;
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
