package nav.statusplattform.core.openingHours;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ServiceDown has det responsibility to tell when a service is down.
 *
 * A service can be down zero, one or many times during the same day.
 *
 * @param from when the service went down
 * @param to when the service returned to ok
 */
record ServiceDown(LocalDateTime from, LocalDateTime to) {

    ServiceDown {
        if (from == null || to == null) {
            throw new IllegalStateException("From and to can not be null.");
        }
        if (!from.toLocalDate().isEqual(to.toLocalDate())) {
            throw new IllegalStateException("From and to needs to be the same day.");
        }
    }

    static ServiceDown from(RecordInterval record, LocalDate actualDay) {
        return new ServiceDown(
                record.getFromDateTime(actualDay),
                record.getToDateTime(actualDay)
        );
    }

    /**
     * Returns the number of minutes the service was down during the opening hours.
     *
     * If it is down before the opening hours, then it will return 0;
     */
    long apply(OpeningHours openingHours) {
        if (serviceIsDownOutsideOfThe(openingHours)) {
            return 0;
        }

        if (serviceIsDownInsideOfThe(openingHours)) {
            return openingHours.openingHoursInMinutes(from, to);
        }

        if (serviceIsDownAll(openingHours)) {
            return openingHours.openingHoursInMinutes();
        }

        if (serviceIsDownDuringOpeningTime(openingHours)) {
            return openingHours.getMinutesTo(to);
        }

        if (serviceIsDownDuringClosingTime(openingHours)) {
            return openingHours.getMinutesFrom(from);
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
    private boolean serviceIsDownOutsideOfThe(OpeningHours openingHours) {
        return to.isBefore(openingHours.openingTime()) || from.isAfter(openingHours.closingTime());
    }

    /**
     * The service was down between opening hours
     */
    private boolean serviceIsDownInsideOfThe(OpeningHours openingHours) {
        return (from.isEqual(openingHours.openingTime()) || from.isAfter(openingHours.openingTime()))
                && (to.isEqual(openingHours.closingTime()) || to.isBefore(openingHours.closingTime()));
    }

    private boolean serviceIsDownAll(OpeningHours openingHours) {
        return from.isBefore(openingHours.openingTime()) && to.isAfter(openingHours.closingTime());
    }

    private boolean serviceIsDownDuringOpeningTime(OpeningHours openingHours) {
        return from.isBefore(openingHours.openingTime()) && to.isAfter(openingHours.openingTime());
    }

    private boolean serviceIsDownDuringClosingTime(OpeningHours openingHours) {
        return from.isBefore(openingHours.closingTime()) && to.isAfter(openingHours.closingTime());
    }
}
