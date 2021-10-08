package no.nav.portal.rest.api.v3.controllers;

import no.nav.portal.rest.api.Helpers.DashboardRepositoryHelper;
import no.portal.web.generated.api.*;
import org.actioncontroller.GET;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.*;


public class TileController {

    private final DashboardRepositoryHelper dashboardRepositoryHelper;


    public TileController(DbContext dbContext) {
        this.dashboardRepositoryHelper = new DashboardRepositoryHelper(dbContext);
    }


    @GET("/Tiles/:Dashboard")
    @JsonBody
    public List<TileDto> getTiles(@PathParam("Dashboard") String dashboardName) {
        return dashboardRepositoryHelper.getTilesOnDashboard(dashboardName);
    }



}
