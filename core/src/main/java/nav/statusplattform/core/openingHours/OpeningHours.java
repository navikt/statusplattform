package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

record OpeningHours(LocalDateTime openingTime, LocalDateTime closingTime) {

    static OpeningHours from(OpeningHoursGroup group, LocalDateTime localDateTime) {
        String openingHoursResult = OpeningHoursParser.getOpeninghours(localDateTime.toLocalDate(), group);

        LocalTime openingTime = OpeningHoursParser.getOpeningTime(openingHoursResult);
        LocalDateTime expectedOpeningTime = localDateTime.withHour(openingTime.getHour()).withMinute(openingTime.getMinute());

        LocalTime closingTime = OpeningHoursParser.getClosingTime(openingHoursResult);
        LocalDateTime expectedClosingTime = localDateTime.withHour(closingTime.getHour()).withMinute(closingTime.getMinute());

        return new OpeningHours(expectedOpeningTime, expectedClosingTime);
    }

    static OpeningHours begin(Map<LocalDate, String> openingHoursMap, LocalDateTime localDateTime) {
        String openingHoursResult = openingHoursMap.get(localDateTime.toLocalDate());

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

        return getMinutes(from, to);
    }

    long openingHoursInMinutes() {
        return getMinutes(openingTime, closingTime);
    }

    long getMinutesFrom(LocalDateTime from) {
        return getMinutes(from, closingTime);
    }

    long getMinutesTo(LocalDateTime to) {
        return getMinutes(openingTime, to);
    }

    /**
     * This method takes into account that the opening hours could be 23:59 and should be threated as next day.
     */
    private long getMinutes(LocalDateTime from, LocalDateTime to) {
        if (to.toLocalTime().equals(LocalTime.of(23, 59))) {
            to = to.plusDays(1).toLocalDate().atStartOfDay();
        }

        long seconds = Duration.between(from, to).getSeconds();
        return Duration.between(from, to).toMinutes();
    }
}
