package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.SampleData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TimelineTest {

    @Test
    public void timeline_should_include_both_start_and_end_of_timespan() {
        Timeline timeline = Timeline.from(new TimeSpan(
                LocalDate.of(2024, 8, 1).atStartOfDay(),
                LocalDate.of(2024, 8, 3).atStartOfDay()));

        assertThat(timeline.days().size()).isEqualTo(3);
    }

    //intertwine -> intergrated - merged - flette sammen

    @Test
    public void test() {
        OpeningHoursRuleEntity rule1 = new OpeningHoursRuleEntity().setRule("17.05.???? ? ? 00:00-00:00"); //National holiday
        OpeningHoursRuleEntity rule3 = new OpeningHoursRuleEntity().setRule("??.??.???? ? ? 07:00-21:00");// Valid for days 1 to 5 or 25 to 30;

        OpeningHoursGroup openingHoursGroup = new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule1, rule3));

        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 1);

        Timeline timeline = Timeline.from(new TimeSpan(
                LocalDate.of(2024, 8, 1).atStartOfDay(),
                LocalDate.of(2024, 8, 3).atStartOfDay()));

        List<ActualExpectedUptime> actualExpectedUptimes = timeline.days().stream()
                .map(localDate -> createDailyUptimeForGivenDay(localDate, records))
                .map(dailyUptime -> dailyUptime.apply(openingHoursGroup))
                .toList();

        assertThat(actualExpectedUptimes.size()).isEqualTo(3);
    }

    private DailyUptime createDailyUptimeForGivenDay(LocalDate currentDay, List<RecordEntity> records) {

        ServiceStatus currentServiceStatus;
        ServiceStatus lastServiceStatus;

        List<ServiceDown> serviceDown = emptyList();

        Iterator<RecordEntity> iterator = records.iterator();
        while (iterator.hasNext()) {
            RecordEntity currentRecord = iterator.next();

            //TODO: Need to consider when the service is stil down at the end of the day - can I use the last record instead?
            currentServiceStatus = currentRecord.getStatus();

            // if currentRecord matches the currentDay
            if (currentDay.isEqual(currentRecord.getCreated_at().toLocalDate())) {
                LocalDateTime startOfServiceDown;
                LocalDateTime endOfServiceDown;

                if (ServiceStatus.OK != currentServiceStatus) {
                    startOfServiceDown = currentRecord.getCreated_at().toLocalDateTime();

                    while (iterator.hasNext()) {
                        RecordEntity nextRecord = iterator.next();

                        System.out.println("NextRecord: " + nextRecord.getCreated_at().toLocalDate());

                        if (ServiceStatus.OK == nextRecord.getStatus()) {
                            if (currentDay.isEqual(nextRecord.getCreated_at().toLocalDate())) {
                                endOfServiceDown = nextRecord.getCreated_at().toLocalDateTime();
                            } else {
                                endOfServiceDown = currentDay.atTime(LocalTime.MIDNIGHT);
                            }

                            serviceDown.add(new ServiceDown(startOfServiceDown, endOfServiceDown));
                        }
                    }
                }
            }
        }

        return new DailyUptime(currentDay, serviceDown);
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
