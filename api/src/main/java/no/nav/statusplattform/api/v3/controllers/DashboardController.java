package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.repositories.DashboardRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.api.Helpers.AreaControllerHelper;
import no.nav.statusplattform.api.Helpers.DashboardControllerHelper;
import no.nav.statusplattform.generated.api.*;
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
    public void updateDashboard(@PathParam("Dashboard_id") UUID dashboard_id, @JsonBody DashboardUpdateDto dashboardUpdateDto) {
        dashboardControllerHelper.updateDashboard(dashboard_id,dashboardUpdateDto);
    }

    @GET("/Dashboard/:Dashboard_id")
    @JsonBody
    public DashboardDto getDashboard(@PathParam("Dashboard_id") UUID dashboard_id) {
        return dashboardControllerHelper.getDashboard(dashboard_id);
    }




}
