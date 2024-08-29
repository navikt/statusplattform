package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DailyUptimeTest {

    @Test
    public void calculate_expected_uptime_when_openinghours_are_730_to_1700() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23),
                List.of(new RecordInterval(
                        LocalDateTime.of(2024, 4, 22, 10, 52),
                        LocalDateTime.of(2024, 4, 24, 14, 21),
                        ServiceStatus.OK)));

        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.expectedUptime()).isEqualTo(570);
    }

    @Test
    public void calculate_actual_uptime_when_openinghours_are_730_to_1700_and_service_is_never_down() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23),
                List.of(new RecordInterval(
                        LocalDateTime.of(2024, 4, 22, 10, 52),
                        LocalDateTime.of(2024, 4, 24, 14, 21),
                        ServiceStatus.OK)));

        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(570);
    }

    //TODO: To fix this, we need to take 23:59 into account
    @Disabled
    @Test
    public void calculate_actual_uptime_when_openinghours_are_00_to_2359_and_service_is_never_down() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23),
                List.of(new RecordInterval(
                        LocalDateTime.of(2024, 4, 22, 10, 52),
                        LocalDateTime.of(2024, 4, 24, 14, 21),
                        ServiceStatus.OK)));

        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_00_00_to_23_59());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(1440);
    }

    @Test
    public void calculate_actual_uptime_when_the_service_is_down_all_opening_hours() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23),
                List.of(new RecordInterval(
                        LocalDate.of(2024, 4, 23).atStartOfDay(),
                        LocalDate.of(2024, 4, 23).atTime(23, 59, 59),
                        ServiceStatus.DOWN)));

        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(0);
    }

    @Test
    public void calculate_actual_uptime_when_the_service_is_down_part_of_opening_hours() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23),
                List.of(new RecordInterval(
                                LocalDate.of(2024, 4, 20).atTime(8, 20),
                                LocalDate.of(2024, 4, 23).atTime(8, 20),
                                ServiceStatus.OK),
                        new RecordInterval(
                                LocalDate.of(2024, 4, 23).atTime(8, 20),
                                LocalDate.of(2024, 4, 23).atTime(8, 25),
                                ServiceStatus.DOWN),
                        new RecordInterval(
                                LocalDate.of(2024, 4, 23).atTime(8, 25),
                                LocalDate.of(2024, 4, 26).atTime(8, 25),
                                ServiceStatus.OK)));

        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(565);
    }

    @Test
    public void calculate_actual_uptime_when_the_service_is_down_more_than_once_during_opening_hours() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23),
                List.of(new RecordInterval(
                                LocalDate.of(2024, 4, 20).atTime(8, 20),
                                LocalDate.of(2024, 4, 23).atTime(8, 20),
                                ServiceStatus.OK),
                        new RecordInterval(
                                LocalDate.of(2024, 4, 23).atTime(8, 20),
                                LocalDate.of(2024, 4, 23).atTime(8, 25),
                                ServiceStatus.DOWN),
                        new RecordInterval(
                                LocalDate.of(2024, 4, 23).atTime(8, 25),
                                LocalDate.of(2024, 4, 23).atTime(10, 20),
                                ServiceStatus.OK),
                        new RecordInterval(
                                LocalDate.of(2024, 4, 23).atTime(10, 20),
                                LocalDate.of(2024, 4, 23).atTime(10, 25),
                                ServiceStatus.DOWN),
                        new RecordInterval(
                                LocalDate.of(2024, 4, 23).atTime(10, 25),
                                LocalDate.of(2024, 4, 26).atTime(10, 25),
                                ServiceStatus.OK)));

        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(560);
    }

    private static OpeningHoursGroup openingHours_7_30_to_17_00() {
        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity().setRule("??.??.???? ? ? 07:30-17:00");
        return new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule));
    }

    private static OpeningHoursGroup openingHours_00_00_to_23_59() {
        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity().setRule("??.??.???? ? ? 00:00-23:59");
        return new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule));
    }
}