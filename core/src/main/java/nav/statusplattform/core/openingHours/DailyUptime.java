package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.summingLong;

record DailyUptime(LocalDate currentDay, List<RecordInterval> recordIntervalsRelatedToCurrentDay) {

    DailyUptime {
        if (recordIntervalsRelatedToCurrentDay == null || recordIntervalsRelatedToCurrentDay.isEmpty()) {
            throw new IllegalStateException("You should not be able to create a daily uptime object without a list of RecordInterval elements.");
        }
    }

    /**
     * When we apply the Opening Hours to the given DailyUptime, we should get an object in return that contains both the
     * expected and actual uptime.
     */
    public ActualExpectedUptime apply(OpeningHoursGroup group) {
        OpeningHours expectedOpeningHours = OpeningHours.from(group, currentDay.atStartOfDay());

        long expectedMinutes = expectedMinutes(expectedOpeningHours);
        long actualMinutes = actualMinutes(expectedOpeningHours, expectedMinutes);

        return new ActualExpectedUptime(expectedMinutes, actualMinutes);
    }

    private long expectedMinutes(OpeningHours expectedOpeningHours) {
        RecordInterval first = recordIntervalsRelatedToCurrentDay.getFirst();
        RecordInterval last = recordIntervalsRelatedToCurrentDay.getLast();

        return expectedOpeningHours.openingHoursInMinutes(
                first.getFromDateTime(currentDay),
                last.getToDateTime(currentDay));
    }

    private long actualMinutes(OpeningHours expectedOpeningHours, long expectedMinutes) {
        long actualDownMinutes = recordIntervalsRelatedToCurrentDay.stream()
                .filter(recordInterval -> recordInterval.isDown())
                .map(recordInterval -> ServiceDown.from(recordInterval, currentDay))
                .map(serviceDown -> serviceDown.apply(expectedOpeningHours))
                .collect(summingLong(Long::longValue));


        return expectedMinutes - actualDownMinutes;
    }
}

record ActualExpectedUptime(long expectedUptime, long actualUptime) {
}
