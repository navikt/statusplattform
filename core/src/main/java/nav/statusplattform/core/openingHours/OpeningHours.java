package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

record OpeningHours(LocalDateTime openingTime, LocalDateTime closingTime) {

    static OpeningHours from(OpeningHoursGroup group, LocalDateTime localDateTime) {
        String openingHoursResult = OpeningHoursParser.getOpeninghours(localDateTime.toLocalDate(), group);

        LocalTime openingTime = OpeningHoursParser.getOpeningTime(openingHoursResult);
        LocalDateTime expectedOpeningTime = localDateTime.withHour(openingTime.getHour()).withMinute(openingTime.getMinute());

        LocalTime closingTime = OpeningHoursParser.getClosingTime(openingHoursResult);
        LocalDateTime expectedClosingTime = localDateTime.withHour(closingTime.getHour()).withMinute(closingTime.getMinute());

        return new OpeningHours(expectedOpeningTime, expectedClosingTime);
    }

    long openingHoursInMinutes(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        LocalDateTime from;
        if (fromDateTime.isAfter(openingTime)) {
            from = fromDateTime;
        } else {
            from = openingTime;
        }

        LocalDateTime to;
        if (toDateTime.isBefore(closingTime)) {
            to = toDateTime;
        } else {
            to = closingTime;
        }

        return Duration.between(from, to).toMinutes();
    }

    long openingHoursInMinutes() {
        return Duration.between(openingTime, closingTime).toMinutes();
    }

    long getMinutes(LocalDateTime from) {
        return Duration.between(from, closingTime).toMinutes();
    }
}
