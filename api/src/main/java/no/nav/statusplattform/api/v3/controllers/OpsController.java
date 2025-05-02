package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.entities.OpsMessageEntity;
import no.nav.statusplattform.api.Helpers.OpsControllerHelper;
import no.nav.statusplattform.generated.api.OPSmessageDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.UUID;

public class OpsController {


    private final OpsControllerHelper opsControllerHelper;

    public OpsController(DbContext dbContext) {
        this.opsControllerHelper = new OpsControllerHelper(dbContext);
    }

    @POST("/OpsMessage")
    @JsonBody
    public OPSmessageDto createOpsMessage(@JsonBody OPSmessageDto opsMessageDto) {
        return opsControllerHelper.newOps(opsMessageDto);
    }

    @GET("/OpsMessage")
    @JsonBody
    public List<OPSmessageDto> getAllOpsMessages() {
        return opsControllerHelper.getAllOpsMessages();
    }

    @PUT("/OpsMessage/:Ops_id")
    @JsonBody
    public OPSmessageDto updateSpecificOpsMessage(@PathParam("Ops_id") UUID ops_id ,@JsonBody OPSmessageDto opsMessageDto) {
        opsMessageDto.setId(ops_id);
        return opsControllerHelper.updateOpsMessage(opsMessageDto);
    }

    @GET("/OpsMessage/:Ops_id")
    @JsonBody
    public OPSmessageDto getSpecificOpsMessage(@PathParam("Ops_id") UUID ops_id ) {
        return opsControllerHelper.getOpsMessage(ops_id);
    }


    @DELETE("/OpsMessage/:Ops_id")
    @JsonBody
    public void deleteOpsMessage(@PathParam("Ops_id") UUID ops_id ) {
        opsControllerHelper.deleteOps(ops_id);
    }


    @GET("/OpsMessage/services")
    @JsonBody
    public List<OPSmessageDto> getOpsMessagesByServiceIds(@RequestParam("service_ids") List<String> serviceIds) {
        return opsControllerHelper.getOpsMessagesByServiceIds(serviceIds);
    }

}
