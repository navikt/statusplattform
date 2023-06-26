package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.RecordRepository;
import org.actioncontroller.GET;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;


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
        return "Status: OK, " +
                "LastGcpPoll: " + recordRepository.getLatestGcpPollTime() +
                ", LastFssPoll: " + recordRepository.getLatestFssPollTime()+
                ", DbStatus: "+dbConnectionStatus;
    }


}
