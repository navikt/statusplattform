package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.openingHours.TimeSpan;
import no.nav.statusplattform.api.Helpers.UpTimeHelper;
import no.nav.statusplattform.generated.api.UpTimeTotalsDto;
import org.actioncontroller.GET;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.UUID;

public class UpTimeController {

    private final UpTimeHelper upTimeHelper;

    public UpTimeController(DbContext dbContext) {
        upTimeHelper = new UpTimeHelper(dbContext);
    }

    @GET("UpTime/:Service_id/:TimeSpan")
    @JsonBody
    @SuppressWarnings("unused")
    public UpTimeTotalsDto getServiceUpTimeSums(@PathParam("Service_id") UUID serviceId, @PathParam("TimeSpan") TimeSpan timeSpan) {
        return upTimeHelper.getServiceUpTimeSums(serviceId, timeSpan);
    }

}
