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

        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity().setRule("??.??.???? ? ? 07:30-17:00");// Valid for any weekday between 7.30am to 5pm
        OpeningHoursGroup group1 = new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule));

        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23), emptyList());
        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(group1);

        assertThat(actualExpectedUptime.expected()).isEqualTo(570);
    }

    @Test
    public void calculate_actual_uptime_for_a_whole_day() {
        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity().setRule("??.??.???? ? ? 07:30-17:00");// Valid for any weekday between 7.30am to 5pm
        OpeningHoursGroup group1 = new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule));

        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23), emptyList());
        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(group1);

        assertThat(actualExpectedUptime.actual()).isEqualTo(570);
    }

    @Test
    public void calculate_actual_uptime_when_the_service_is_down_all_opening_hours() {
        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity().setRule("??.??.???? ? ? 07:30-17:00");// Valid for any weekday between 7.30am to 5pm
        OpeningHoursGroup group1 = new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule));

        DailyUptime dailyUptime = new DailyUptime(LocalDate.of(2024, 4, 23), List.of(
                new ServiceDown(
                        LocalDate.of(2024, 4, 23).atStartOfDay(),
                        LocalDate.of(2024, 4, 23).atTime(23, 59, 59))));
        ActualExpectedUptime actualExpectedUptime = dailyUptime.apply(group1);

        assertThat(actualExpectedUptime.actual()).isEqualTo(0);


    }
}
