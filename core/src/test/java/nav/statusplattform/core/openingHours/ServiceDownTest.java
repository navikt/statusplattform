package nav.statusplattform.core.openingHours;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceDownTest {

    private final DailyUptime.OpeningHours OPENING_HOURS_8_16 = new DailyUptime.OpeningHours(
            LocalDateTime.of(2024, 8, 10, 8, 0),
            LocalDateTime.of(2024, 8, 10, 16, 0));

    @Test
    public void serviceIsDownBeforeOpeningHours() {
        ServiceDown serviceDown = new ServiceDown(
                LocalDateTime.of(2024, 8, 10, 5, 41),
                LocalDateTime.of(2024, 8, 10, 7, 10));

        long minutesDowntime = serviceDown.apply(OPENING_HOURS_8_16);

        assertThat(minutesDowntime).isEqualTo(0);
    }

    @Test
    public void serviceIsDownAfterOpeningHours() {
        ServiceDown serviceDown = new ServiceDown(
                LocalDateTime.of(2024, 8, 10, 16, 41),
                LocalDateTime.of(2024, 8, 10, 17, 10));

        long minutesDowntime = serviceDown.apply(OPENING_HOURS_8_16);

        assertThat(minutesDowntime).isEqualTo(0);
    }

    @Test
    public void serviceIsDownInsideOfOpneningHours() {
        ServiceDown serviceDown = new ServiceDown(
                LocalDateTime.of(2024, 8, 10, 13, 41),
                LocalDateTime.of(2024, 8, 10, 14, 10));

        long minutesDowntime = serviceDown.apply(OPENING_HOURS_8_16);

        assertThat(minutesDowntime).isEqualTo(29);
    }

    @Test
    public void serviceIsDownEqualToOpneningHours() {
        ServiceDown serviceDown = new ServiceDown(
                LocalDateTime.of(2024, 8, 10, 8, 0),
                LocalDateTime.of(2024, 8, 10, 16, 0));

        long minutesDowntime = serviceDown.apply(OPENING_HOURS_8_16);

        assertThat(minutesDowntime).isEqualTo(480);
    }

    @Test
    public void serviceIsDownWhenOpeningHoursStart() {
        ServiceDown serviceDown = new ServiceDown(
                LocalDateTime.of(2024, 8, 10, 7, 41),
                LocalDateTime.of(2024, 8, 10, 9, 10));

        long minutesDowntime = serviceDown.apply(OPENING_HOURS_8_16);

        assertThat(minutesDowntime).isEqualTo(70);
    }

    @Test
    public void serviceIsDownWhenOpeningHoursCloses() {
        ServiceDown serviceDown = new ServiceDown(
                LocalDateTime.of(2024, 8, 10, 15, 41),
                LocalDateTime.of(2024, 8, 10, 17, 10));

        long minutesDowntime = serviceDown.apply(OPENING_HOURS_8_16);

        assertThat(minutesDowntime).isEqualTo(19);
    }
}
