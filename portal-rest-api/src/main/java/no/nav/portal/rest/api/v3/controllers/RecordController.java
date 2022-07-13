package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.RecordRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.actioncontroller.GET;
import org.actioncontroller.HttpRequestException;
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
    private AlertDto currentAlert;

    public RecordController(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
    }


    @POST("/ServiceStatus")
    public  void addServiceStatus(@JsonBody RecordDto recordDto){
        //TODO denne må utbedres
        ServiceStatus status = ServiceStatus.fromDb(recordDto.getStatus().getValue().toUpperCase())
                .orElseThrow(() -> new HttpRequestException("Could not parse status: "+ recordDto.getStatus() +
                        " should be on format: OK, ISSUE or DOWN"));
        RecordEntity entity = new RecordEntity()
                .setServiceId(recordDto.getServiceId())
                .setStatus(status)
                .setDescription(recordDto.getDescription())
                .setCreated_at(ZonedDateTime.now())
                .setLogglink(recordDto.getLogLink())
                .setResponsetime(42);//TODO se her
        recordRepository.save(entity);
    }

    @GET("/ServiceStatus/:Service_id")
    @JsonBody
    public List<RecordDto> getRecordHistory(@PathParam("Service_id") UUID service_id) {
        return EntityDtoMappers.toRecordDto(
                recordRepository.getRecordHistory(service_id,100));
    }

    @POST("/Alert")
    public  void postAlert(@JsonBody AlertDto test){
        currentAlert = test;
        System.out.println("HER KOMMER ALERT: ");
        System.out.println(test.getStatus());
        System.out.println(test.getCommonLabels().getAlertname());
        System.out.println(test.getCommonAnnotations().getSeverity());
        RecordDto recordDto = generateServiceStatusFromAlert(test);
        addServiceStatus(recordDto);

    }

    @GET("/Alert")
    @JsonBody
    public AlertDto getAlert(){
        return currentAlert;
    }



    private RecordDto generateServiceStatusFromAlert(AlertDto alertDto){
        if(AlertStatusDto.RESOLVED.equals(alertDto.getStatus())){
            return new RecordDto()
                    .serviceId(alertDto.getCommonLabels().getAlertname())
                    .status(StatusDto.OK)
                    .description("Resolved alert from prometheus");
        }
        if(AlertSeverityDto.GOOD.equals(alertDto.getCommonAnnotations().getSeverity())){
            return new RecordDto()
                    .serviceId(alertDto.getCommonLabels().getAlertname())
                    .status(StatusDto.OK)
                    .description("Received alert from prometheus, with status ok");

        }
        if(AlertSeverityDto.WARNING.equals(alertDto.getCommonAnnotations().getSeverity())){
            return new RecordDto()
                    .serviceId(alertDto.getCommonLabels().getAlertname())
                    .status(StatusDto.ISSUE)
                    .description("Received alert from prometheus, with status warning");

        }
        if(AlertSeverityDto.DANGER.equals(alertDto.getCommonAnnotations().getSeverity())){
            return new RecordDto()
                    .serviceId(alertDto.getCommonLabels().getAlertname())
                    .status(StatusDto.DOWN)
                    .description("Received alert from prometheus, with status danger");

        }
        else throw new IllegalArgumentException("Malformed alert received");

    }





}
