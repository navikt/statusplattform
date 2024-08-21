package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.SampleData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TimelineTest {

    @Test
    public void timeline_should_include_both_start_and_end_of_timespan() {
        TimeSpan timeSpan = new TimeSpan(
                LocalDate.of(2024, 8, 1).atStartOfDay(),
                LocalDate.of(2024, 8, 3).atStartOfDay());

        Timeline timeline = timeSpan.toTimeline();

        assertThat(timeline.days()).hasSize(3);
    }

    //intertwine -> intergrated - merged - flette sammen

    @Test
    public void test() {
        OpeningHoursRuleEntity rule1 = new OpeningHoursRuleEntity().setRule("17.05.???? ? ? 00:00-00:00"); //National holiday
        OpeningHoursRuleEntity rule3 = new OpeningHoursRuleEntity().setRule("??.??.???? ? ? 07:00-21:00");// Valid for days 1 to 5 or 25 to 30;

        OpeningHoursGroup openingHoursGroup = new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule1, rule3));

        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        Records records = new Records(generateRandomizedRecordEntities(service, 1));

        TimeSpan timeSpan = new TimeSpan(
                LocalDate.of(2024, 8, 1).atStartOfDay(),
                LocalDate.of(2024, 8, 3).atStartOfDay());

        Timeline timeline = timeSpan.toTimeline();

        List<ActualExpectedUptime> actualExpectedUptimes = timeline.actualExpectedUptimesFrom(records, openingHoursGroup);

        assertThat(actualExpectedUptimes).hasSize(3);
        assertThat(actualExpectedUptimes).contains(new ActualExpectedUptime(840, 840));

        ActualExpectedUptime totalActualExpectedUptime = new ActualExpectedUptime(0, 0);
        actualExpectedUptimes.stream()
                .map(actualExpectedUptime -> totalActualExpectedUptime.pluss(actualExpectedUptime));

    }

    private List<RecordEntity> generateRandomizedRecordEntities(ServiceEntity serviceEntity, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The argument amount refers to size of list, must be a non-zero, positive integer.");
        }
        List<RecordEntity> records = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            RecordEntity randomizedRecordEntityForService = SampleData.getRandomizedRecordEntityForService(serviceEntity);
            randomizedRecordEntityForService.setCreated_at(LocalDate.of(2024, 8, 1).atStartOfDay().atZone(ZoneId.systemDefault()));
            randomizedRecordEntityForService.setStatus(ServiceStatus.OK);
            records.add(randomizedRecordEntityForService);
        }
        return records;
    }
}
