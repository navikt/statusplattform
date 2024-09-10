package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.openingHours.TimeSpan;
import nav.statusplattform.core.openingHours.UpTimeTotals;
import no.nav.statusplattform.api.Helpers.UpTimeHelper;

import org.actioncontroller.GET;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;

import java.util.UUID;

public class UpTimeController {
    private UpTimeHelper upTimeHelper;

    public UpTimeController(UpTimeHelper upTimeHelper) {
        this.upTimeHelper = upTimeHelper;
    }

    @GET("UpTime/:Service_id/:TimeSpan")
    @JsonBody
    public UpTimeTotalsDto getServiceUpTimeSums(@PathParam("Service_id") UUID service_id, @PathParam("TimeSpan") TimeSpan timeSpan) {
        UpTimeTotalsDto upTimeTotalsDto = UpTimeHelper.getServiceUpTimeSums(service_id, timeSpan);
        return upTimeTotalsDto;
    }

}
