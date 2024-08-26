package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.summingLong;

record DailyUptime(LocalDate dateTime, List<ServiceDown> serviceDowns) {

    public ActualExpectedUptime apply(OpeningHoursGroup group) {
        OpeningHours expectedOpeningHours = OpeningHours.from(group, dateTime.atStartOfDay());

        long expectedMinutes = Duration.between(
                expectedOpeningHours.openingTime(),
                expectedOpeningHours.closingTime()
        ).toMinutes();

        long actualDownMinutes = serviceDowns.stream()
                    .map(serviceDown -> serviceDown.apply(expectedOpeningHours))
                    .collect(summingLong(Long::longValue));

        long actualMinutes = expectedMinutes - actualDownMinutes;

        return new ActualExpectedUptime(expectedMinutes, actualMinutes);
    }
}

record ActualExpectedUptime(long expectedUptime, long actualUptime) {
}
