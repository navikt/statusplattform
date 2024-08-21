package nav.statusplattform.core.openingHours;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActualExpectedUptimeTest {

    @Test
    public void skal_kunne_legge_sammen_to_ActualExpectedUptime() {
        ActualExpectedUptime actualExpectedUptime_A = new ActualExpectedUptime(500, 400);
        ActualExpectedUptime actualExpectedUptime_B = new ActualExpectedUptime(241, 102);

        ActualExpectedUptime totalActualExpectedUptime = actualExpectedUptime_A.pluss(actualExpectedUptime_B);

        assertThat(totalActualExpectedUptime.expectedUptime()).isEqualTo(741);
        assertThat(totalActualExpectedUptime.actualUptime()).isEqualTo(502);
    }
}
