package nav.statusplattform.core.openingHours;

public class ActualExpectedUptime {

    private long expectedUptime;
    private long actualUptime;

    ActualExpectedUptime(long expectedUptime, long actualUptime) {
        this.expectedUptime = expectedUptime;
        this.actualUptime = actualUptime;
    }
    public ActualExpectedUptime pluss(ActualExpectedUptime other) {
        this.expectedUptime += other.expectedUptime;
        this.actualUptime += other.actualUptime;

        return this;
    }

    public long actualUptime() {
        return actualUptime;
    }

    public long expectedUptime() {
        return expectedUptime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActualExpectedUptime that = (ActualExpectedUptime) o;

        if (expectedUptime != that.expectedUptime) return false;
        return actualUptime == that.actualUptime;
    }

    @Override
    public int hashCode() {
        int result = (int) (expectedUptime ^ (expectedUptime >>> 32));
        result = 31 * result + (int) (actualUptime ^ (actualUptime >>> 32));
        return result;
    }
}
