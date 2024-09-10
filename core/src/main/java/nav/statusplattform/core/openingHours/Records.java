package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a list of RecordIntervals and the given timespan.
 *
 * This is another way of looking at the RecordEntities, where each interval is closed.
 */
record Records(List<RecordInterval> intervals, TimeSpan timeSpan) {

    Records {
        if (intervals == null || intervals.isEmpty()) {
            throw new IllegalStateException("The list of RecordInterval can´t be empty.");
        }
    }

    /**
     * Iterates through all days during a timespan, comparing the dates and times, applying
     * respective opening hours for each RecordIntervals.
     */
    List<ActualExpectedUptime> apply(OpeningHoursGroup openingHoursGroup) {
        return timeSpan.allDaysIncludingStartAndEndDate().stream()
                .map(DayDuringTimeline::new)
                .map(dayDuringTimeline -> dayDuringTimeline.dailyUptimeFrom(this))
                .map(dailyUptime -> dailyUptime.apply(openingHoursGroup))
                .toList();
    }


    /* Returns a chronological list of events within a given period containing data about a service's
    start date/time and availability. */
    static Records fromRecordEntities(List<RecordEntity> records, TimeSpan timeSpan) {
        if (records.isEmpty()) {
            throw new IllegalStateException("There has to be at least one record in the list.");
        }

        RecordsBuilder recordsBuilder = new RecordsBuilder().init(records.get(0));

        for (int i=1; i < records.size(); i++) {
            RecordEntity currentRecord = records.get(i);
            recordsBuilder.append(currentRecord);
        }

        return new Records(recordsBuilder.build(timeSpan.to()), timeSpan);
    }

    private static class RecordsBuilder {

        private ServiceStatus status;
        private LocalDateTime localDateTime;

        private List<RecordInterval> intervals = new ArrayList<>();

        RecordsBuilder init(RecordEntity recordEntity) {
            RecordsBuilder recordsBuilder = new RecordsBuilder();
            recordsBuilder.localDateTime = recordEntity.getCreated_at().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);
            recordsBuilder.status = recordEntity.getStatus();
            return recordsBuilder;
        }

        void append(RecordEntity currentRecord) {
            RecordInterval recordInterval = new RecordInterval(localDateTime, currentRecord.getCreated_at().toLocalDateTime(), status);
            this.intervals.add(recordInterval);
            this.localDateTime = currentRecord.getCreated_at().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);
            this.status = currentRecord.getStatus();
        }

        List<RecordInterval> build(LocalDateTime endOfTimeSpan) {
            RecordInterval recordInterval = new RecordInterval(this.localDateTime, endOfTimeSpan.truncatedTo(ChronoUnit.SECONDS), status);
            this.intervals.add(recordInterval);
            return intervals;
        }
    }
}

/**
 * Record Interval contains the from and to for a given ServiceStatus. The "from" is created from when an event is
 * happening (like turn into UP, DOWN, UNKNOWN etc) and the "to" is created from when the next event is happening.
 *
 * If there is no next event, then the "to" is equal to the end of the timespan.
 */
record RecordInterval(LocalDateTime from, LocalDateTime to, ServiceStatus serviceStatus) {

    /**
     * If the from-date is equal to the actual day, we can use the given time for that day.
     * If not, we assume the event happened some day before and use the start of the day as the given time.
     */
    LocalDateTime getFromDateTime(LocalDate actualDay) {
        LocalDateTime from;
        if (this.from.toLocalDate().isEqual(actualDay)) {
            from = this.from;
        } else {
            from = actualDay.atStartOfDay();
        }
        return from;
    }

    LocalDateTime getToDateTime(LocalDate actualDay) {
        LocalDateTime to;
        if (this.to.toLocalDate().isEqual(actualDay)) {
            to = this.to;
        } else {
            to = actualDay.atTime(LocalTime.MAX);
        }
        return to;
    }

    boolean isValidFor(LocalDate actualDay) {
        return (actualDay.isEqual(from.toLocalDate()) || actualDay.isAfter(from.toLocalDate())
                && (actualDay.isEqual(to.toLocalDate()) || actualDay.isBefore(to.toLocalDate())));
    }

    boolean isDown() {
        return serviceStatus != ServiceStatus.OK;
    }
}