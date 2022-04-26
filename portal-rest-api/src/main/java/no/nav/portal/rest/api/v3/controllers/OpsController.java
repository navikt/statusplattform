package no.nav.portal.rest.api.v3.controllers;

import no.nav.portal.rest.api.Helpers.OpsControllerHelper;
import no.nav.portal.rest.api.Helpers.ServiceControllerHelper;
import no.portal.web.generated.api.OPSmessageDto;
import org.actioncontroller.POST;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

public class OpsController {


    private OpsControllerHelper opsControllerHelper;

    public OpsController(DbContext dbContext) {
        this.opsControllerHelper = new OpsControllerHelper(dbContext);
    }

    @POST("/OpsMessage")
    @JsonBody
    public OPSmessageDto createOpsMessage(@JsonBody OPSmessageDto opsMessageDto) {
        return opsControllerHelper.newOps(opsMessageDto);
    }
}
