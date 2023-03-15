package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.RecordRepository;
import no.portal.web.generated.api.RecordDto;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.Optional;

public class RecordControllerHelper {

    private final RecordRepository recordRepository;



    public RecordControllerHelper(DbContext dbContext) {
        this.recordRepository = new RecordRepository(dbContext);
    }
    public void deleteRecordsOlderThan48Hours(){
        recordRepository.deleteRecordsOlderThan48hours();
    }

    public void updateRecords(List<RecordDto> dtos){
        dtos.forEach(this::updateRecordForService);
    }


    private void updateRecordForService(RecordDto recordDto){

        Optional<RecordEntity> latestDiffRecord = recordRepository.getLatestRecordDiff(recordDto.getServiceId());
        RecordEntity newRecord = mapToRecordEntity(recordDto);

        //Dersom ny status er forskjellig fra gammel, legges det til en status_diff
        if(latestDiffRecord.isEmpty() || !latestDiffRecord.get().getStatus().equals(newRecord.getStatus())){
            newRecord.setActive(true);
            recordRepository.saveNewStatusDiff(newRecord);
            //Setter den gamle til inaktiv
            latestDiffRecord.ifPresent(recordRepository::saveOldStatusDiff);
        }
        else{
            //Hvis ikke økes teller på status
            recordRepository.increaseCountOnStatusDiff(latestDiffRecord.get());

        }
        recordRepository.save(newRecord);
    }
    private RecordEntity mapToRecordEntity(RecordDto recordDto) {
        return new RecordEntity()
                .setServiceId(recordDto.getServiceId())
                .setStatus(ServiceStatus.valueOf(recordDto.getStatus().getValue()))
                .setDescription(recordDto.getDescription())
                .setLogglink(recordDto.getLogLink())
                .setCreated_at(recordDto.getTimestamp().toZonedDateTime())
                .setResponsetime(recordDto.getResponseTime());
    }






}
