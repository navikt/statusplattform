package no.nav.portal.rest.api.v3.controllers;

import com.unboundid.ldap.sdk.unboundidds.AlertSeverity;
import nav.portal.core.entities.RecordEntity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.RecordRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
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
    private AlertDto currentAlert;

    public RecordController(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
    }


    @POST("/ServiceStatus")
    public  void addServiceStatus(@JsonBody ServiceStatusDto serviceStatusDto){
        //TODO denne må utbedres
        RecordEntity entity = new RecordEntity()
                .setServiceId(serviceStatusDto.getServiceId())
                .setStatus(ServiceStatus.fromDb(serviceStatusDto.getStatus().getValue().toUpperCase()).orElse(ServiceStatus.ISSUE))
                .setDescription(serviceStatusDto.getDescription())
                .setCreated_at(ZonedDateTime.now())
                .setResponsetime(42);//TODO se her
        recordRepository.save(entity);
    }

    @GET("/ServiceStatus/:Service_id")
    @JsonBody
    public List<ServiceStatusDto> getRecordHistory(@PathParam("Service_id") UUID service_id) {
        return EntityDtoMappers.toServiceStatusDto(
                recordRepository.getRecordHistory(service_id,100));
    }

    @POST("/Alert/test")
    public  void postAlert(@JsonBody AlertDto test){
        currentAlert = test;
        System.out.println("HER KOMMER ALERT: ");
        System.out.println(test.getStatus());
        System.out.println(test.getCommonLabels().getAlertname());
        System.out.println(test.getCommonAnnotations().getSeverity());
        ServiceStatusDto serviceStatusDto = generateServiceStatusFromAlert(test);
        addServiceStatus(serviceStatusDto);

    }

    @GET("/Alert/test")
    @JsonBody
    public AlertDto getAlert(){
        return currentAlert;
    }



    private ServiceStatusDto generateServiceStatusFromAlert(AlertDto alertDto){
        if(AlertStatusDto.RESOLVED.equals(alertDto.getStatus())){
            return new ServiceStatusDto()
                    .serviceId(alertDto.getCommonLabels().getAlertname())
                    .status(StatusDto.OK)
                    .description("Resolved alert from prometheus");
        }
        if(SeverityDto.GOOD.equals(alertDto.getCommonAnnotations().getSeverity())){
            return new ServiceStatusDto()
                    .serviceId(alertDto.getCommonLabels().getAlertname())
                    .status(StatusDto.OK)
                    .description("Received alert from prometheus, with status ok");

        }
        if(SeverityDto.WARNING.equals(alertDto.getCommonAnnotations().getSeverity())){
            return new ServiceStatusDto()
                    .serviceId(alertDto.getCommonLabels().getAlertname())
                    .status(StatusDto.ISSUE)
                    .description("Received alert from prometheus, with status warning");

        }
        if(SeverityDto.DANGER.equals(alertDto.getCommonAnnotations().getSeverity())){
            return new ServiceStatusDto()
                    .serviceId(alertDto.getCommonLabels().getAlertname())
                    .status(StatusDto.DOWN)
                    .description("Received alert from prometheus, with status danger");

        }
        else throw new IllegalArgumentException("Malformed alert received");

    }





}
