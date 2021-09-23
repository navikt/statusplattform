package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.DashboardDto;
import org.actioncontroller.GET;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    private final AreaRepository areaRepository;
    private final DashboardRepository dashboardRepository;
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;


    public DashboardController(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }

    @GET("/Dashboards")
    @JsonBody
    public List<DashboardDto> getDashboards() {
        return dashboardRepository.retrieveAll().stream()
                .map(EntityDtoMappers::toDto)
                .collect(Collectors.toList());
    }
}
