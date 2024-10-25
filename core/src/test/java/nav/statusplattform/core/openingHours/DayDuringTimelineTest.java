package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.enums.ServiceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DayDuringTimelineTest {

    @Test
    public void dailyUptimeFrom_should_only_return_intervals_that_is_related_to_the_given_day() {
        Records records = new Records(
                List.of(new RecordInterval(
                                LocalDateTime.of(2024, 5, 1, 10, 52),
                                LocalDateTime.of(2024, 6, 1, 14, 21),
                                ServiceStatus.OK),
                        new RecordInterval(
                                LocalDateTime.of(2024, 6, 1, 14, 21),
                                LocalDateTime.of(2024, 7, 1, 10, 52),
                                ServiceStatus.DOWN),
                        new RecordInterval(
                                LocalDateTime.of(2024, 7, 1, 10, 52),
                                LocalDateTime.of(2024, 8, 1, 14, 21),
                                ServiceStatus.OK),
                        new RecordInterval(
                                LocalDateTime.of(2024, 8, 1, 14, 21),
                                LocalDateTime.of(2024, 9, 1, 10, 52),
                                ServiceStatus.DOWN)),
                new TimeSpan(
                        LocalDate.of(2024, 7, 1).atTime(11, 14, 3),
                        LocalDate.of(2024, 8, 2).atTime(14, 13)));

        DayDuringTimeline dayDuringTimeline = new DayDuringTimeline(LocalDate.of(2024, 8, 1));

        DailyUptime dailyUptime = dayDuringTimeline.dailyUptimeFrom(records);

        assertThat(dailyUptime.recordIntervalsRelatedToCurrentDay()).containsExactly(
                new RecordInterval(
                        LocalDateTime.of(2024, 7, 1, 10, 52),
                        LocalDateTime.of(2024, 8, 1, 14, 21),
                        ServiceStatus.OK),
                new RecordInterval(
                        LocalDateTime.of(2024, 8, 1, 14, 21),
                        LocalDateTime.of(2024, 9, 1, 10, 52),
                        ServiceStatus.DOWN));
    }
}