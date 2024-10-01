package nav.statusplattform.core.openingHours;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeSpanTest {

    @Test
    public void timeline_should_include_both_start_and_end_of_timespan() {
        TimeSpan timeSpan = new TimeSpan(
                LocalDate.of(2024, 8, 1).atStartOfDay(),
                LocalDate.of(2024, 8, 3).atStartOfDay());

        assertThat(timeSpan.allDaysIncludingStartAndEndDate()).hasSize(3);
    }
}
