package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.util.UuidListBody;
import no.nav.portal.infrastructure.PortalRestPrincipal;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.AreaRepositoryHelper;
import no.nav.portal.rest.api.Helpers.DashboardRepositoryHelper;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.DashboardDto;

import no.portal.web.generated.api.DashboardNameIdDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DashboardController {

    private final DashboardRepository dashboardRepository;
    private final DashboardRepositoryHelper dashboardRepositoryHelper;
    private AreaRepositoryHelper areaRepositoryHelper;


    public DashboardController(DbContext dbContext) {
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.dashboardRepositoryHelper = new DashboardRepositoryHelper(dbContext);
        this.areaRepositoryHelper = new AreaRepositoryHelper(dbContext);

    }


    @GET("/Dashboards")
    @JsonBody
    public List<DashboardNameIdDto> getDashboards() {
        return EntityDtoMappers.toDashboardDtoShallow(dashboardRepository.getAllDashboardUUIDsAndNames());
    }

    @POST("/Dashboard")
    @JsonBody
    public UUID postDashboard(@JsonBody DashboardDto dashboardDto) {
        UUID uuid = dashboardRepository.save(dashboardDto.getName());
        dashboardRepository.settAreasOnDashboard(uuid,
                dashboardDto.getAreas().stream().map(
                        AreaDto::getId).collect(Collectors.toList()));
        return uuid;
    }

    @DELETE("/Dashboard/:Dashboard_id")
    @JsonBody
    public void deleteDashboard(@PathParam("Dashboard_id") UUID dashboard_id) {
        dashboardRepositoryHelper.deleteDashboard(dashboard_id);
    }


    @PUT("/Dashboard/:Dashboard_id")
    public void addAreaToDashboard(@PathParam("Dashboard_id") UUID dashboard_id, @UuidListBody List<UUID> areaIds) {
        dashboardRepository.settAreasOnDashboard(dashboard_id, areaIds);
    }

    @PUT("/Dashboard/Update/:Dashboard_id")
    public void updateNameOfDashboard(@PathParam("Dashboard_id") UUID dashboard_id, @JsonBody DashboardDto dashboard) {
        dashboardRepository.updateNameOfDashboard(dashboard_id, dashboard.getName());
    }

    @GET("/Dashboard/:Dashboard_id")
    @JsonBody
    public DashboardDto getAreas(@PathParam("Dashboard_id") UUID dashboard_id) {
        return dashboardRepositoryHelper.getDashboard(dashboard_id);
    }




}
