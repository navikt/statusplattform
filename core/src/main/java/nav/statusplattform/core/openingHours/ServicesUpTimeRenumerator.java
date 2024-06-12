package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.RecordRepository;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

import static java.time.LocalDate.*;
import static java.time.temporal.ChronoUnit.MINUTES;

public class ServicesUpTimeRenumerator {
    static RecordRepository recordRepository;
    public static final int MINUTES_IN_A_HOUR = 60;
    private static Set<Integer> weekdaysList;


    public static long calculateUpTimeForService(UUID serviceId, LocalDate DateEntryFrom, LocalDate DateEntryTo, String rule) {
        String[] ruleParts = "[\s]".split(rule);
        if (!isWeekDaysRuleApplicable(ruleParts[2])) {
            throw new IllegalStateException("Arguments for weekdays must be of of numeric values, representing 1 for Monday to 7 for Sunday");
        }

        //ruleParts[3] represents the services opening hours
        return checkAndGetCalculationForUpTimePercentage(serviceId, DateEntryFrom, DateEntryTo, ruleParts[3]);
    }

    private static boolean isWeekDaysRuleApplicable(String weekDayRule) {

        //Checks for ?
        if (weekDayRule.equals("?")) {
            weekdaysList = Set.of(1, 2, 3, 4, 5, 6, 7);
            return true;
        }

        //int dayOfWeekNumber = dateTimeEntry.getDayOfWeek().getValue();

        weekdaysList = new HashSet<>();

        //Checks for a singular week day in the month or for a range
        String[] ruleParts = weekDayRule.split("[,]");

        int lowerRange;
        int upperRange;

        //check the range/s for a weekday number match
        for (String rulePart : ruleParts) {
            if (rulePart.contains("-")) {
                //checks weekday falls within a range
                lowerRange = Integer.parseInt(rulePart.substring(0, 1));
                upperRange = Integer.parseInt(rulePart.substring(2));
                for (int i = lowerRange; i < upperRange; i++) {
                    weekdaysList.add(i);
                }
            } else {
                //checks weekday value matches a single weekday value
                weekdaysList.add(Integer.parseInt(rulePart));
            }
        }
        return !weekdaysList.isEmpty();
    }


    public static long checkAndGetCalculationForUpTimePercentage(UUID serviceId, LocalDate DateEntryFrom, LocalDate DateEntryTo, String openingHours) {

        //checks for yyyy-MM-dd
        if (DateEntryFrom == null || DateEntryTo == null) {
            throw new IllegalStateException("Arguments for DateEntry must have a format pattern of 'yyyy-MM-dd'");
        }

        //checks if the date entries are greater than the current date
        if (DateEntryFrom.isAfter(now()) || DateEntryTo.isAfter(now())) {
            throw new IllegalStateException("Arguments for DateEntry cannot be greater than the current date");
        }

        //checks for hh:mm-hh:mm
        if (openingHours == null) {
            throw new IllegalStateException("The argument openingHours must have a format pattern of 'hh:mm-hh:mm'");
        }

        //Application is always down
        if (openingHours.equals("00:00-00:00")) {
            return 0;
        }

        //Application is always up
        if (openingHours.equals("00:00-23:59")) {
            openingHours = "00:00-24:00";
        }

        String[] ruleParts = openingHours.split("[-]");

        //Obtain the time in hh:mm format
        String[] ohStartTime = ruleParts[0].split("[:]"); //start time hours[0] and minutes[1]
        String[] ohEndTime = ruleParts[1].split("[:]"); //end time hours[0] and minutes[1]

        //Obtain time in LocalDateFormat
        LocalTime ohLocalTimeStart = LocalTime.of(Integer.parseInt(ohStartTime[0]), Integer.parseInt(ohStartTime[1]));
        LocalTime ohLocalTimeEnd = LocalTime.of(Integer.parseInt(ohEndTime[0]), Integer.parseInt(ohEndTime[1]));

        //Obtain current tme of request
        LocalTime currentTime = LocalTime.now();

        //Obtained zonedDateTime
        ZonedDateTime zonedDateTimeFrom = ZonedDateTime.of(DateEntryFrom, ohLocalTimeStart, (ZoneId.of("Europe/Oslo")));
        ZonedDateTime zonedDateTimeTo = ZonedDateTime.of(DateEntryTo, ohLocalTimeEnd, (ZoneId.of("Europe/Oslo")));

        final int dailyOpeningHours = getDailyOpeningHours(ohStartTime, ohEndTime);

        //Total opening hours in minutes
        long totalOpeningTimeInMinutes = calculateTotalOpeningTime(DateEntryFrom, DateEntryTo, dailyOpeningHours, ohLocalTimeStart,
                ohLocalTimeEnd, currentTime);


        //Total down time for service in minutes
        long totalDownTimeInMinutes = calculateServiceDownTimeForPeriod(serviceId, zonedDateTimeFrom, zonedDateTimeTo);

        /* Percentage uptime of service  is  the services uptime total - total down time total divided up time times by 100 */
        return (totalOpeningTimeInMinutes - totalDownTimeInMinutes) / totalDownTimeInMinutes * 100;
    }

    public static long calculateTotalOpeningTime(LocalDate DateEntryFrom, LocalDate DateEntryTo, int dailyOpeningHours, LocalTime ohLocalTimeStart,
                                                 LocalTime ohLocalTimeEnd, LocalTime currentTime) {
        //Calculate total number of minutes in a period from fromDateEntry to toDateEntry
        int allDaysBetween = (int) (ChronoUnit.DAYS.between(DateEntryFrom, DateEntryTo) + 1);
        long allOpeningHourMinutes = IntStream.range(0, allDaysBetween)
                .filter(i -> isWorkingDay(DateEntryFrom.plusDays(i)))
                .count() * dailyOpeningHours;


        long unlapsedTimeinCurrentDate = 0;

        /*If the request includes the current date and falls within the services opening hours only the time
         lapsed from opening hours start time is included as part of the calculation. The lapsed time is removed*/
        if (DateEntryTo.isEqual(LocalDate.now())) { //check for input entryDate falls on same day as current date
            //Check that time of request is after opening hours start but before opening hours end
            if (currentTime.isAfter(ohLocalTimeStart) && currentTime.isBefore(ohLocalTimeEnd)) {
                unlapsedTimeinCurrentDate = MINUTES.between(currentTime, ohLocalTimeEnd);
            }
        }

        return allOpeningHourMinutes - unlapsedTimeinCurrentDate;

    }

    public static long calculateServiceDownTimeForPeriod(UUID serviceId, ZonedDateTime zonedDateTimeFrom,
                                                        ZonedDateTime zonedDateTimeTo) {
        List<RecordEntity> recordEntities =
                recordRepository.getRecordHistoryWithinPeriod(serviceId, zonedDateTimeFrom, zonedDateTimeTo);

        long sumDownTime = 0L;

        for (int i = 0; i < recordEntities.size() - 1; i++) {
            if (recordEntities.get(i).getStatus() == ServiceStatus.DOWN) {
                sumDownTime += zonedDateTimeDifference(recordEntities.get(i).getCreated_at(), recordEntities.get(i + 1).getCreated_at());
            }

            //What if the last record status is down?
        }
        return sumDownTime;
    }

    private static int getDailyOpeningHours(String[] openingString, String[] closingString) {
        LocalTime openingTime = LocalTime.of(Integer.parseInt(openingString[0]), Integer.parseInt(openingString[1]));
        LocalTime closingTime = LocalTime.of(Integer.parseInt(closingString[0]), Integer.parseInt(closingString[1]));

        int startHour = Integer.parseInt(openingString[0]);
        int endHour = Integer.parseInt(closingString[0]);


        //Check if opening hours start is greater than end time
        if (openingTime.isAfter(closingTime)) {
            throw new IllegalStateException("Opening hours end time must be greater than start time");
        }

        // hours [0] and mins [1]
        int closingTimeMinutes = (int) ((Integer.parseInt(closingString[0]) * MINUTES_IN_A_HOUR) + Integer.parseInt(closingString[1]));
        int openingTimeMinutes = (int) ((Integer.parseInt(openingString[0]) * MINUTES_IN_A_HOUR) + Integer.parseInt(openingString[1]));


        //Total number of minutes in a opening times
        int totalOpeningHours = closingTimeMinutes - openingTimeMinutes;
        return totalOpeningHours;
    }

    public static boolean isWorkingDay(final LocalDate date) {
        return weekdaysList.contains(date.getDayOfWeek().getValue());
    }

    static long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2) {
        return ChronoUnit.MINUTES.between(d1, d2);
    }

}
