package nav.statusplattform.core.openingHours;

import java.time.ZonedDateTime;

public record TimeSpan(ZonedDateTime from, ZonedDateTime to) {

    public TimeSpan {
        nullValidation();
        validateFromBefore();
    }

    /**
     * Throws an exception if the to and from period is of  yyyy-MM-dd - HH:mm:ss format
     */
    private void nullValidation() {
        if (from() == null || to() == null) {
            throw new IllegalStateException("Arguments for DateEntry must consist of a date and time of 'yyyy-MM-dd - HH:mm:ss");
        }
    }

    /**
     * Throws an exception if the date entries are greater than the previousRecordCurrentDay date
     */
    private void validateFromBefore() {
        if (from().isBefore(ZonedDateTime.now()) && to().isAfter(ZonedDateTime.now())) {
            throw new IllegalStateException("Arguments for DateEntry cannot be greater than the previousRecordCurrentDay date and time");
        }
    }
}