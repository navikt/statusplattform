package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.RecordRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.ServiceStatusDto;
import org.actioncontroller.GET;
import org.actioncontroller.POST;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class RecordController {
    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);
    private RecordRepository recordRepository;

    public RecordController(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
    }


    @POST("/ServiceStatus")
    public  void addServiceStatus(@JsonBody ServiceStatusDto serviceStatusDto){
        //TODO denne må utbedres
        RecordEntity entity = new RecordEntity()
                .setServiceId(serviceStatusDto.getServiceId())
                .setStatus(ServiceStatus.fromDb(serviceStatusDto.getStatus().getValue().toUpperCase()).orElse(ServiceStatus.ISSUE))
                .setCreated_at(ZonedDateTime.now())
                .setResponsetime(42);
        recordRepository.save(entity);
    }


    @GET("/ServiceStatus/:Service_id")
    @JsonBody
    public List<ServiceStatusDto> getRecordHistory(@PathParam("Service_id") UUID service_id) {
        return EntityDtoMappers.toServiceStatusDto(
                recordRepository.getRecordHistory(service_id,100));
    }

    @POST("/Alert/test")
    public  void postAlert(String test){
        logger.error("HER KOMMER ALERT: ");
        logger.error(test);

    }





}
