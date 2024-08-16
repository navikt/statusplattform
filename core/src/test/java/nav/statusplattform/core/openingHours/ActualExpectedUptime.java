package nav.statusplattform.core.openingHours;

public class ActualExpectedUptime {

    private final long expectedUptime;
    private final long actualUptime;

    public ActualExpectedUptime(long expectedUptime, long actualUptime) {
        this.expectedUptime = expectedUptime;
        this.actualUptime = actualUptime;
    }

    public long expected() {
        return expectedUptime;
    }

    public long actual() {
        return actualUptime;
    }
}
