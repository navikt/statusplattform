package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.SampleData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RecordsTest {

    @Test
    public void converting_an_empty_list_of_recordEntity_should_throw_an_exception() {
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> Records.fromRecordEntities(emptyList(), new TimeSpan(LocalDate.of(2024, 7, 1).atTime(11, 14, 3), LocalDate.of(2024, 8, 1).atTime(14, 13))));
        assertThat(illegalStateException.getMessage()).isEqualTo("There has to be at least one record in the list.");
    }

    @Test
    public void converting_list_of_one_should_result_in_list_of_one() {
        Records records = Records.fromRecordEntities(
                List.of(
                        getRecordEntity(LocalDate.of(2024, 8, 1).atStartOfDay(), ServiceStatus.DOWN)
                ), new TimeSpan(
                        LocalDate.of(2024, 7, 1).atTime(11, 14, 3),
                        LocalDate.of(2024, 8, 1).atTime(14, 13)));

        List<RecordInterval> recordInterval = records.intervals();

        assertThat(recordInterval).contains(
                new RecordInterval(
                        LocalDate.of(2024, 8, 1).atStartOfDay(),
                        LocalDate.of(2024, 8, 1).atTime(14, 13),
                        ServiceStatus.DOWN));
    }

    @Test
    public void converts_a_list_of_recordEntity_to_a_list_of_recordIntervals() {
        Records records = Records.fromRecordEntities(
                List.of(
                        getRecordEntity(LocalDate.of(2024, 8, 1).atStartOfDay(), ServiceStatus.DOWN),
                        getRecordEntity(LocalDate.of(2024, 8, 3).atStartOfDay(), ServiceStatus.OK)
                ), new TimeSpan(
                        LocalDate.of(2024, 7, 1).atTime(11, 14, 3),
                        LocalDate.of(2024, 8, 1).atTime(14, 13)));

        List<RecordInterval> recordInterval = records.intervals();

        assertThat(recordInterval).containsExactly(
                new RecordInterval(
                        LocalDate.of(2024, 8, 1).atStartOfDay(),
                        LocalDate.of(2024, 8, 3).atStartOfDay(),
                        ServiceStatus.DOWN),
                new RecordInterval(
                        LocalDate.of(2024, 8, 3).atStartOfDay(),
                        LocalDate.of(2024, 8, 1).atTime(14, 13),
                        ServiceStatus.OK));
    }

    private static RecordEntity getRecordEntity(LocalDateTime createdAt, ServiceStatus serviceStatus) {
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        RecordEntity randomizedRecordEntityForService = SampleData.getRandomizedRecordEntityForService(serviceEntity);
        randomizedRecordEntityForService.setCreated_at(createdAt.atZone(ZoneId.systemDefault()));
        randomizedRecordEntityForService.setStatus(serviceStatus);
        return randomizedRecordEntityForService;
    }
}
