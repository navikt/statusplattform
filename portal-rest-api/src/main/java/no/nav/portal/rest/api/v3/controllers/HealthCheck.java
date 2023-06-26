package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.RecordRepository;
import org.actioncontroller.GET;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;


public class HealthCheck {
    private final AreaRepository areaRepository;
    private final RecordRepository recordRepository;


    public HealthCheck(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }

    @GET("/isAlive")
    @JsonBody
    public String getDashboards() {
        boolean dbConnectionStatus = !areaRepository.retriveAllShallow().isEmpty();
        ZonedDateTime lastUpdateGcp = recordRepository.getLatestGcpPollTime().plusHours(2).truncatedTo(ChronoUnit.SECONDS);
        ZonedDateTime lastUpdateFss = recordRepository.getLatestFssPollTime().plusHours(2).truncatedTo(ChronoUnit.SECONDS);
        return "Status: OK, " +
                "LastGcpPoll: " + lastUpdateGcp +
                ", LastFssPoll: " + lastUpdateFss +
                ", DbStatus: "+dbConnectionStatus;
    }


}
