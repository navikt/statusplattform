package nav.statusplattform.core.openingHours;

import java.time.Duration;
import java.time.LocalDateTime;

public record ServiceDown(LocalDateTime from, LocalDateTime to) {

    /**
     * Returns the number of minutes the service was down during the opening hours.
     *
     * If it is down before the opening hours, then it will return 0;
     */
    public long apply(DailyUptime.OpeningHours openingHours) {
        if (serviceIsDownOutsideOfThe(openingHours)) {
            return 0;
        }

        if (serviceIsDownInsideOfThe(openingHours)) {
            return Duration.between(from, to).toMinutes();
        }

        if (serviceIsDownAll(openingHours)) {
            return Duration.between(openingHours.openingTime(), openingHours.closingTime()).toMinutes();
        }

        if (serviceIsDownDuringOpeningTime(openingHours)) {
            return Duration.between(openingHours.openingTime(), to).toMinutes();
        }

        if (serviceIsDownDuringClosingTime(openingHours)) {
            return Duration.between(from, openingHours.closingTime()).toMinutes();
        }

        throw new IllegalStateException("We are missing a use case when the service is down from %s to %s and the opening hours are %s and %s"
                .formatted(from, to, openingHours.openingTime(), openingHours.closingTime()));
    }

    /**
     * The service was down before or after the opening hours.<br>
     * <br>
     * Either the end of service downtime is before start of opening hours,
     * or the start of service downtime is after the end of opening hours.
     */
    private boolean serviceIsDownOutsideOfThe(DailyUptime.OpeningHours openingHours) {
        return to.isBefore(openingHours.openingTime()) || from.isAfter(openingHours.closingTime());
    }

    /**
     * The service was down between opening hours
     */
    private boolean serviceIsDownInsideOfThe(DailyUptime.OpeningHours openingHours) {
        return (from.isEqual(openingHours.openingTime()) || from.isAfter(openingHours.openingTime()))
                && (to.isEqual(openingHours.closingTime()) || to.isBefore(openingHours.closingTime()));
    }

    private boolean serviceIsDownAll(DailyUptime.OpeningHours openingHours) {
        return from.isBefore(openingHours.openingTime()) && to.isAfter(openingHours.closingTime());
    }

    private boolean serviceIsDownDuringOpeningTime(DailyUptime.OpeningHours openingHours) {
        return from.isBefore(openingHours.openingTime()) && to.isAfter(openingHours.openingTime());
    }

    private boolean serviceIsDownDuringClosingTime(DailyUptime.OpeningHours openingHours) {
        return from.isBefore(openingHours.closingTime()) && to.isAfter(openingHours.closingTime());
    }
}
