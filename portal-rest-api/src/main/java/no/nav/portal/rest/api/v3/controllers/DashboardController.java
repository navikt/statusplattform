package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.util.UuidListBody;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.AreaControllerHelper;
import no.nav.portal.rest.api.Helpers.DashboardControllerHelper;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.DashboardDto;

import no.portal.web.generated.api.DashboardNameIdDto;
import no.portal.web.generated.api.IdContainerDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DashboardController {

    private final DashboardRepository dashboardRepository;
    private final DashboardControllerHelper dashboardControllerHelper;
    private AreaControllerHelper areaControllerHelper;


    public DashboardController(DbContext dbContext) {
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.dashboardControllerHelper = new DashboardControllerHelper(dbContext);
        this.areaControllerHelper = new AreaControllerHelper(dbContext);
    }

    @GET("/Dashboards")
    @JsonBody
    public List<DashboardNameIdDto> getDashboards() {
        return EntityDtoMappers.toDashboardDtoShallow(dashboardRepository.getAllDashboardUUIDsAndNames());
    }

    @POST("/Dashboard")
    @JsonBody
    public IdContainerDto postDashboard(@JsonBody DashboardDto dashboardDto) {
        UUID uuid = dashboardRepository.save(dashboardDto.getName());
        dashboardRepository.settAreasOnDashboard(uuid,
                dashboardDto.getAreas().stream().map(
                        AreaDto::getId).collect(Collectors.toList()));
        return new IdContainerDto().id(uuid);
    }

    @DELETE("/Dashboard/:Dashboard_id")
    @JsonBody
    public void deleteDashboard(@PathParam("Dashboard_id") UUID dashboard_id) {
        dashboardControllerHelper.deleteDashboard(dashboard_id);
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
        return dashboardControllerHelper.getDashboard(dashboard_id);
    }




}
