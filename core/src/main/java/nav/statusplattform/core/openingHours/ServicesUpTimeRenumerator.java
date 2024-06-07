package nav.statusplattform.core.openingHours;

import com.sun.source.tree.IfTree;
import nav.statusplattform.core.entities.RecordEntity;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.time.LocalDate.*;
import static java.time.temporal.ChronoUnit.MINUTES;

public class ServicesUpTimeRenumerator {

    public static final int MINUTES_IN_A_HOUR = 60;


    /*public static long calculateUpTimeForService(UUID serviceId, LocalDate DateEntryFrom, LocalDate DateEntryTo, String openingHours) {


    }*/


    public static long getTotalOpeningHoursMinutes(LocalDate DateEntryFrom, LocalDate DateEntryTo, String openingHours) {

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
        return calculateTotalOpeningTime(DateEntryFrom, DateEntryTo, dailyOpeningHours, ohLocalTimeStart,
                ohLocalTimeEnd, currentTime);

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

    /*public static int calculateServiceDownTimeForPeriod(UUID serviceId, zonedDateTimeFrom, zonedDateTimeTo) {
        List<RecordEntity> recordEntities = getRecordHistoryWithinPeriod(UUID serviceId, ZonedDateTime from, ZonedDateTime to)

    }*/

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
        return date.getDayOfWeek().getValue() < DayOfWeek.SATURDAY.getValue();
    }

}
