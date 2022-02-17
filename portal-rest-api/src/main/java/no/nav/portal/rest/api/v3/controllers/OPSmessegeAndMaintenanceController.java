package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.RecordRepository;
import no.nav.portal.rest.api.Helpers.ServiceRepositoryHelper;
import no.portal.web.generated.api.MaintenanceDto;
import no.portal.web.generated.api.OPSmessageDto;
import no.portal.web.generated.api.ServiceDto;
import org.actioncontroller.POST;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

public class OPSmessegeAndMaintenanceController {


    private ServiceRepositoryHelper serviceRepositoryHelper;

    public OPSmessegeAndMaintenanceController(DbContext dbContext) {
        this.serviceRepositoryHelper = new ServiceRepositoryHelper(dbContext);
    }

    @POST("/OpsMessage")
    @JsonBody
    public OPSmessageDto createOpsMessage(@JsonBody OPSmessageDto opsMessageDto) {
        return serviceRepositoryHelper.addOPSmessega(opsMessageDto);
    }


    @POST("/Maintenance")
    @JsonBody
    public MaintenanceDto createMaintenanceMessage(@JsonBody MaintenanceDto maintenanceDto) {
        return serviceRepositoryHelper.setMaintenance(maintenanceDto);
    }



}
