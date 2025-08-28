package nav.statusplattform.core.openingHours;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Wrapper class for a given day.
 *
 * Gives us the ability to apply a method to the class which could be tested in isolation.
 */
record DayDuringTimeline(LocalDate actualDay) {

    DailyUptime dailyUptimeFrom(Records records) {
        List<RecordInterval> recordIntervals = records.intervals()
                .stream()
                .filter(record -> record.isValidFor(actualDay))
                .collect(toList());

        return new DailyUptime(actualDay, recordIntervals);
    }
}