package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.RecordDeltaEntity;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.RecordSource;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.RecordRepository;
import nav.statusplattform.core.repositories.ServiceRepository;
import no.nav.statusplattform.generated.api.RecordDto;
import org.fluentjdbc.DbContext;
import org.jsonbuddy.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class RecordControllerHelper {

    private static final Logger logger = LoggerFactory.getLogger(RecordControllerHelper.class);

    private final RecordRepository recordRepository;
    private final ServiceRepository serviceRepository;
    private NotificationQueueHelper notificationQueueHelper;

    public RecordControllerHelper(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.notificationQueueHelper = new NotificationQueueHelper(dbContext);
    }
    public void deleteRecordsOlderThan48Hours(){
        recordRepository.deleteRecordsOlderThan48hours();
    }

    public void deleteDeltaRecordsOlderThanThreeYears() {
        recordRepository.deleteDeltaRecordsOlderThanThreeYears();
    }

    public void updateRecords(List<RecordDto> dtos){
        dtos.forEach(this::updateRecordForService);
    }


    public void updateRecordForService(RecordDto recordDto){

        Optional<RecordDeltaEntity> latestDiffRecord = recordRepository.getLatestRecordDiff(recordDto.getServiceId());
        RecordEntity newRecord = mapToRecordEntity(recordDto);

        //Dersom ny status er forskjellig fra gammel, legges det til en status_diff
        if(latestDiffRecord.isEmpty() || !latestDiffRecord.get().getStatus().equals(newRecord.getStatus())){
            newRecord.setActive(true);
            recordRepository.saveNewStatusDiff(newRecord);
            //Setter den gamle til inaktiv
            latestDiffRecord.ifPresent(recordRepository::setOldStatusDiffInactive);

            // Queue notification for status change
            if (latestDiffRecord.isPresent()) {
                try {
                    ServiceStatus oldStatus = latestDiffRecord.get().getStatus();
                    ServiceStatus newStatus = newRecord.getStatus();
                    String serviceName = serviceRepository.retrieve(newRecord.getServiceId())
                            .map(ServiceEntity::getName).orElse("Ukjent tjeneste");
                    notificationQueueHelper.queueStatusChangeNotifications(
                            newRecord.getServiceId(), serviceName, oldStatus, newStatus);
                } catch (Exception e) {
                    logger.error("Failed to queue status change notification for service {}", newRecord.getServiceId(), e);
                }
            }
        }
        else{
            //Hvis ikke økes teller på status
            recordRepository.increaseCountOnStatusDiff(latestDiffRecord.get());

        }
        recordRepository.save(newRecord);
    }
    private RecordEntity mapToRecordEntity(RecordDto recordDto) {
        RecordSource source = recordDto.getSource()== null? RecordSource.UNKNOWN: RecordSource.valueOf(recordDto.getSource().getValue());
        return new RecordEntity()
                .setServiceId(recordDto.getServiceId())
                .setStatus(ServiceStatus.valueOf(recordDto.getStatus().getValue()))
                .setDescription(recordDto.getDescription())
                .setLogglink(recordDto.getLogLink())
                .setCreated_at(recordDto.getTimestamp().toZonedDateTime())
                .setResponsetime(recordDto.getResponseTime())
                .setRecordSource(source);
    }


}
