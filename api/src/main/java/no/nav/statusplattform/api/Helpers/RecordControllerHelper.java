package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.RecordDeltaEntity;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.RecordSource;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.RecordRepository;
import no.nav.statusplattform.generated.api.RecordDto;
import org.fluentjdbc.DbContext;
import org.jsonbuddy.JsonObject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RecordControllerHelper {

    private final RecordRepository recordRepository;



    public RecordControllerHelper(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
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
        }
        else{
            //Hvis ikke økes teller på status
            recordRepository.increaseCountOnStatusDiff(latestDiffRecord.get());

        }
        recordRepository.save(newRecord);
    }

    public List<JsonObject> getAllStatusesFromStatusholder(List<JsonObject> body) {
        // Fetch descriptions for each status holder
        return body.stream().map(status -> {
            UUID serviceId = UUID.fromString(status.requiredString("serviceId"));
            Optional<RecordEntity> recordEntity = recordRepository.getLatestRecord(serviceId);
            String description = recordEntity.map(RecordEntity::getDescription).orElse("");
            return status.put("description", description);
        }).collect(Collectors.toList());
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
