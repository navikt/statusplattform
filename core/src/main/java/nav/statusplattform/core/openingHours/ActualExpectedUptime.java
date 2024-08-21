package nav.statusplattform.core.openingHours;

public record ActualExpectedUptime(long expectedUptime, long actualUptime) {

    public ActualExpectedUptime pluss(ActualExpectedUptime other) {
        return new ActualExpectedUptime(
                this.expectedUptime + other.expectedUptime,
                this.actualUptime + other.actualUptime);
    }
}
