package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.AreaWithServices;
import nav.portal.core.entities.DashboardEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.DashboardRepositoryHelper;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.DashboardDto;
import no.portal.web.generated.api.TileDto;
import org.actioncontroller.DELETE;
import org.actioncontroller.GET;
import org.actioncontroller.POST;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DashboardController {

    private final DashboardRepository dashboardRepository;
    private final DashboardRepositoryHelper dashboardRepositoryHelper;


    public DashboardController(DbContext dbContext) {
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.dashboardRepositoryHelper = new DashboardRepositoryHelper(dbContext);

    }

    @GET("/Dashboards")
    @JsonBody
    public List<DashboardDto> getDashboards() {
        return EntityDtoMappers.toDashboardDto(dashboardRepository.retrieveAll());

    }
    @POST("/Dashboard/:Dashboard")
    @JsonBody
    public void addAreaToDashboard(@JsonBody AreaDto areaDto, @PathParam("Dashboard") String dashboard) {
        UUID dashboardId = dashboardRepository.uidFromName(dashboard);
        dashboardRepository.addAreaToDashboard(dashboardId,areaDto.getId());
    }

    @DELETE("/Dashboard/:Dashboard")
    @JsonBody
    public void removeAreaFromDashboard(@JsonBody AreaDto areaDto, @PathParam("Dashboard") String dashboard) {
        UUID dashboardId = dashboardRepository.uidFromName(dashboard);
        dashboardRepository.removeAreaFromDashboard(dashboardId,areaDto.getId());
    }

    @GET("/Tiles/:Dashboard")
    @JsonBody
    public List<TileDto> getTiles(@PathParam("Dashboard") String dashboardName) {
        return dashboardRepositoryHelper.getTilesOnDashboard(dashboardName);
    }

}
