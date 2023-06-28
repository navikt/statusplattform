package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.RecordRepository;
import org.actioncontroller.GET;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;



public class HealthCheck {
    private final AreaRepository areaRepository;
    private final RecordRepository recordRepository;


    public HealthCheck(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }

    @GET("/isAlive")
    @JsonBody
    public String isAlive() {
        boolean dbConnectionStatus = !areaRepository.retriveAllShallow().isEmpty();
        ZonedDateTime zonedGcp = recordRepository.getLatestGcpPollTime();
        ZonedDateTime zonedFss = recordRepository.getLatestFssPollTime();
        LocalDateTime lastUpdateGcp = zonedGcp!=null? zonedGcp.plusHours(2).truncatedTo(ChronoUnit.SECONDS).toLocalDateTime() :null;
        LocalDateTime lastUpdateFss = zonedFss!= null? zonedFss.plusHours(2).truncatedTo(ChronoUnit.SECONDS).toLocalDateTime(): null;
        return "Status: OK, " +
                "LastGcpPoll: " + lastUpdateGcp +
                ", LastFssPoll: " + lastUpdateFss +
                ", DbStatus: "+dbConnectionStatus;
    }


}
