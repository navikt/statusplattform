package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.openingHours.TimeSpan;
import no.nav.statusplattform.api.Helpers.UpTimeHelper;
import no.nav.statusplattform.generated.api.UpTimeTotalsDto;
import org.actioncontroller.GET;
import org.actioncontroller.PathParam;
import org.actioncontroller.RequestParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class UpTimeController {

    private static final Logger LOG = LoggerFactory.getLogger(UpTimeController.class);

    private final UpTimeHelper upTimeHelper;

    public UpTimeController(DbContext dbContext) {
        upTimeHelper = new UpTimeHelper(dbContext);
    }

    @GET("/UpTime/test")
    @SuppressWarnings("unused")
    @JsonBody
    public String helloWorld() {
        LOG.error("Hello, world!");
        return "Hello, world!";
    }

    @GET("/UpTime/:Service_id?:From&:To")
    @JsonBody
    @SuppressWarnings("unused")
    public UpTimeTotalsDto getServiceUpTimeSums(
            @PathParam("Service_id") String serviceId,
            @RequestParam("From") String from,
            @RequestParam("To") String to
    ) {
        LOG.info("I'm here!");
        var timeSpan = new TimeSpan(parse(from), parse(to));
        LOG.info("timeSpan: {},{} -> {}", from, to, timeSpan);
        return upTimeHelper.getServiceUpTimeSums(UUID.fromString(serviceId), timeSpan);
    }

    private static LocalDateTime parse(String value)
        throws DateTimeParseException {
        // TODO: Make sure we don't end up with a HTTP 500.
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(value, formatter);
    }

}
