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
        ZonedDateTime lastUpdateGcp = recordRepository.getLatestGcpPollTime();
        ZonedDateTime lastUpdateFss = recordRepository.getLatestFssPollTime();
        return "Status: OK, " +
                "LastGcpPoll: " + OffsetDateTime.of(lastUpdateGcp.toLocalDateTime(),lastUpdateGcp.getOffset()).truncatedTo(ChronoUnit.SECONDS) +
                ", LastFssPoll: " + OffsetDateTime.of(lastUpdateFss.toLocalDateTime(),lastUpdateFss.getOffset()).truncatedTo(ChronoUnit.SECONDS) +
                ", DbStatus: "+dbConnectionStatus;
    }


}
