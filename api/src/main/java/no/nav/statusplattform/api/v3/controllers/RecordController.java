package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.RecordRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.api.Helpers.RecordControllerHelper;
import no.nav.statusplattform.generated.api.AlertDto;
import no.nav.statusplattform.generated.api.AlertSeverityDto;
import no.nav.statusplattform.generated.api.AlertStatusDto;
import no.nav.statusplattform.generated.api.RecordDto;
import no.nav.statusplattform.generated.api.StatusDto;
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
    private RecordControllerHelper recordControllerHelper;
    private AlertDto currentAlert;

    public RecordController(DbContext dbContext) {
        this.recordControllerHelper = new RecordControllerHelper(dbContext);
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

    @POST("/UpdateRecords")
    public  void updateRecords(@JsonBody List<RecordDto> recordDtos){
        //Endpoint used by poller job, see https://github.com/navikt/statusportal-gcp-poll.git
        recordControllerHelper.updateRecords(recordDtos);
        recordControllerHelper.deleteRecordsOlderThan48Hours();
        recordControllerHelper.deleteDeltaRecordsOlderThanThreeYears();
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
    }

    @GET("/Alert")
    @JsonBody
    public AlertDto getAlert(){
        if(currentAlert != null){
            return currentAlert;
        }
        return new AlertDto();
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
