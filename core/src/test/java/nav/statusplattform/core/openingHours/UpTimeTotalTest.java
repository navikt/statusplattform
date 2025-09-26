package nav.statusplattform.core.openingHours;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UpTimeTotalTest {

    @Test
    public void skal_kunne_legge_sammen_to_ActualExpectedUptime() {
        ActualExpectedUptime actualExpectedUptime_A = new ActualExpectedUptime(500, 400);
        ActualExpectedUptime actualExpectedUptime_B = new ActualExpectedUptime(241, 102);

        UpTimeTotals upTimeTotals = UpTimeTotals.sumOfAll(List.of(actualExpectedUptime_A, actualExpectedUptime_B));

        assertThat(upTimeTotals.sumOfExpectedUptime()).isEqualTo(741);
        assertThat(upTimeTotals.sumOfActualUptime()).isEqualTo(502);
    }
}
