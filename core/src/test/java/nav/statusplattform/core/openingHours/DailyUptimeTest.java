package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DailyUptimeTest {

    @Test
    public void calculate_expected_uptime_for_a_whole_day() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23), emptyList());
        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.expectedUptime()).isEqualTo(570);
    }

    @Test
    public void calculate_actual_uptime_for_a_whole_day_without_any_downtime() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23), emptyList());
        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(570);
    }

    @Test
    public void calculate_actual_uptime_when_the_service_is_down_all_opening_hours() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23), List.of(
                new ServiceDown(
                        LocalDate.of(2024, 4, 23).atStartOfDay(),
                        LocalDate.of(2024, 4, 23).atTime(23, 59, 59))));
        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(0);
    }

    @Test
    public void calculate_actual_uptime_when_the_service_is_down_part_of_opening_hours() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23), List.of(
                new ServiceDown(
                        LocalDate.of(2024, 4, 23).atTime(8, 20),
                        LocalDate.of(2024, 4, 23).atTime(8, 25))));
        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(565);
    }

    @Test
    public void calculate_actual_uptime_when_the_service_is_down_more_than_once_during_opening_hours() {
        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23), List.of(
                new ServiceDown(
                        LocalDate.of(2024, 4, 23).atTime(8, 20),
                        LocalDate.of(2024, 4, 23).atTime(8, 25)),
                new ServiceDown(
                        LocalDate.of(2024, 4, 23).atTime(10, 20),
                        LocalDate.of(2024, 4, 23).atTime(10, 25))));
        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(openingHours_7_30_to_17_00());

        assertThat(actualExpectedUptime.actualUptime()).isEqualTo(560);
    }

    private static OpeningHoursGroup openingHours_7_30_to_17_00() {
        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity().setRule("??.??.???? ? ? 07:30-17:00");
        return new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule));
    }
}