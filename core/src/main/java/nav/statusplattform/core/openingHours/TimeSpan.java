package nav.statusplattform.core.openingHours;

import java.time.ZonedDateTime;

public record TimeSpan(ZonedDateTime from, ZonedDateTime to) {

    public TimeSpan {
        if (from == null || to == null) {
            throw new IllegalStateException("Arguments for DateEntry must consist of a date and time of 'yyyy-MM-dd - HH:mm:ss");
        }
        if (from.isBefore(ZonedDateTime.now()) && to.isAfter(ZonedDateTime.now())) {
            throw new IllegalStateException("Arguments for DateEntry cannot be greater than the previousRecordCurrentDay date and time");
        }
    }

}