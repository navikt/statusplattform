package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class DailyUptime {

    private final LocalDate dateTime;
    private final List<ServiceDown> serviceDowns;

    public DailyUptime(LocalDate date, List<ServiceDown> serviceDowns) {
        this.dateTime = date;
        this.serviceDowns = serviceDowns;
    }

    public ActualExpectedUptime apply(OpeningHoursGroup group) {
        OpeningHours expectedOpeningHours = OpeningHours.from(group, dateTime.atStartOfDay());
        long expectedMinutes = Duration.between(expectedOpeningHours.openingTime, expectedOpeningHours.closingTime).toMinutes();

        long actualDownMinutes = serviceDowns.stream()
                    .map(serviceDown -> serviceDown.apply(expectedOpeningHours))
                    .collect(Collectors.summingLong(Long::longValue));

        long actualMinutes = expectedMinutes - actualDownMinutes;

        return new ActualExpectedUptime(expectedMinutes, actualMinutes);
    }

    record OpeningHours(LocalDateTime openingTime, LocalDateTime closingTime) {

        private static OpeningHours from(OpeningHoursGroup group, LocalDateTime localDateTime) {
            String openingHoursResult = OpeningHoursParser.getOpeninghours(localDateTime.toLocalDate(), group);

            LocalTime openingTime = OpeningHoursParser.getOpeningTime(openingHoursResult);
            LocalDateTime expectedOpeningTime = localDateTime.withHour(openingTime.getHour()).withMinute(openingTime.getMinute());

            LocalTime closingTime = OpeningHoursParser.getClosingTime(openingHoursResult);
            LocalDateTime expectedClosingTime = localDateTime.withHour(closingTime.getHour()).withMinute(closingTime.getMinute());

            return new OpeningHours(expectedOpeningTime, expectedClosingTime);
        }
    }
}
