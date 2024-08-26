package nav.statusplattform.core.openingHours;

import java.util.List;
import java.util.Objects;

public final class UpTimeTotal {
    private long sumOfActualUptime;
    private long sumOfExpectedUptime;

    private UpTimeTotal(long sumOfActualUptime, long sumOfExpectedUptime) {
        this.sumOfActualUptime = sumOfActualUptime;
        this.sumOfExpectedUptime = sumOfExpectedUptime;
    }

    public static UpTimeTotal sumOfAll(List<ActualExpectedUptime> actualExpectedUptimes) {
        UpTimeTotal upTimeTotal = new UpTimeTotal(0, 0);
        actualExpectedUptimes.stream()
                .forEach(actualExpectedUptime -> upTimeTotal.pluss(actualExpectedUptime));

        return upTimeTotal;
    }

    private UpTimeTotal pluss(ActualExpectedUptime actualExpectedUptime) {
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
        var that = (UpTimeTotal) obj;
        return this.sumOfActualUptime == that.sumOfActualUptime &&
                this.sumOfExpectedUptime == that.sumOfExpectedUptime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sumOfActualUptime, sumOfExpectedUptime);
    }

    @Override
    public String toString() {
        return "UpTimeTotal[" +
                "sumOfActualUptime=" + sumOfActualUptime + ", " +
                "sumOfExpectedUptime=" + sumOfExpectedUptime + ']';
    }
}
