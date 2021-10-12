package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.DashboardRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.DashboardRepositoryHelper;
import no.portal.web.generated.api.DashboardDto;
import no.portal.web.generated.api.TileDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;
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
        return EntityDtoMappers.toDashboardDtoShallow(dashboardRepository.getAllDashboardUUIDsAndNames());
    }

    @POST("/Dashboards/:Dashboard_name")
    @JsonBody
    public UUID postDashboard(@PathParam("Dashboard_name") String dashboard_name) {
        return dashboardRepository.save(dashboard_name);
    }


    @PUT("/Dashboard/:Dashboard_id")
    @JsonBody
    public void addAreaToDashboard(@PathParam("Dashboard_id") UUID dashboard_id,@JsonBody List<UUID> areaIds) {
        dashboardRepository.settAreasOnDashboard(dashboard_id,areaIds);
    }

    @GET("/Dashboard/:Dashboard_id")
    @JsonBody
    public List<TileDto> getDashboard(@PathParam("Dashboard_id") UUID dashboard_id) {
        return dashboardRepositoryHelper.getTilesOnDashboard(dashboard_id);
    }


}
