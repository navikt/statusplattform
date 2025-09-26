package nav.statusplattform.core.openingHours;

import java.util.List;
import java.util.Objects;

public final class UpTimeTotals {
    private long sumOfActualUptime;
    private long sumOfExpectedUptime;

    private UpTimeTotals(long sumOfActualUptime, long sumOfExpectedUptime) {
        this.sumOfActualUptime = sumOfActualUptime;
        this.sumOfExpectedUptime = sumOfExpectedUptime;
    }

    static UpTimeTotals sumOfAll(List<ActualExpectedUptime> actualExpectedUptimes) {
        UpTimeTotals upTimeTotals = new UpTimeTotals(0, 0);
        actualExpectedUptimes.stream()
                .forEach(actualExpectedUptime -> upTimeTotals.pluss(actualExpectedUptime));

        return upTimeTotals;
    }

    private UpTimeTotals pluss(ActualExpectedUptime actualExpectedUptime) {
        this.sumOfExpectedUptime += actualExpectedUptime.expectedUptime();
        this.sumOfActualUptime += actualExpectedUptime.actualUptime();
        return this;
    }
    
    public long sumOfActualUptime() {
        return sumOfActualUptime;
    }

    public long sumOfExpectedUptime() {
        return sumOfExpectedUptime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UpTimeTotals) obj;
        return this.sumOfActualUptime == that.sumOfActualUptime &&
                this.sumOfExpectedUptime == that.sumOfExpectedUptime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sumOfActualUptime, sumOfExpectedUptime);
    }

    @Override
    public String toString() {
        return "UpTimeTotals[" +
                "sumOfActualUptime=" + sumOfActualUptime + ", " +
                "sumOfExpectedUptime=" + sumOfExpectedUptime + ']';
    }
}
