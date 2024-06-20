package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.RecordRepository;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

import static java.time.LocalDate.*;

public class ServicesUpTimeRenumerator {

    static RecordRepository recordRepository;

    public static long calculateUpTimeForService(UUID serviceId, LocalDate DateEntryFrom, LocalDate DateEntryTo, String rule) {
        String[] ruleParts = rule.split("[ ]");

        String openingHours = ruleParts[3];

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

        String[] openingHoursParts = openingHours.split("-");

        //Obtain the time in hh:mm format
        String[] ohStartTime = openingHoursParts[0].split(":"); //start time hours[0] and minutes[1]
        String[] ohEndTime = openingHoursParts[1].split(":"); //end time hours[0] and minutes[1]

        //Convert string to local date format to get  LocalDateFormat that can be used to obtain zone datetime
        LocalTime oHStartTime = LocalTime.of(Integer.parseInt(ohStartTime[0]), Integer.parseInt(ohStartTime[1]));
        LocalTime oHEndTime = LocalTime.of(Integer.parseInt(ohEndTime[0]), Integer.parseInt(ohEndTime[1]));

        //Obtained zonedDateTime
        ZonedDateTime from = ZonedDateTime.of(DateEntryFrom, oHStartTime, (ZoneId.of("Europe/Oslo")));
        ZonedDateTime to = ZonedDateTime.of(DateEntryTo, oHEndTime, (ZoneId.of("Europe/Oslo")));

        return calculatePercentageUptime(serviceId, from, to);

    }

    public static long calculatePercentageUptime(UUID serviceId, ZonedDateTime from, ZonedDateTime to) {
        // These records Have to be sorted in chronological order

        List<RecordEntity> records = recordRepository.getRecordsInTimeSpan(serviceId, from, to);

        long sumOfActualUptime = 0; //Represents the total uptime for service for example,  service status equals
        long sumOfExpectedUptime = 0; //Represents the total time

        RecordEntity previousRecord = records.getFirst();

        //Sum up (A) all the time  service has been UP, and all the time service should have been up
        for (RecordEntity currentRecord : records.subList(1, records.size())) {

            // Get the summarized amount of minutes of uptime specified within the timespan that the opening hours rule(s) expect
            //Obtains the time difference between two periods
            long expectedOpeningHoursTimeSpan;

            int allDaysBetween = (int) (ChronoUnit.DAYS.between(from, to) + 1);

            expectedOpeningHoursTimeSpan = IntStream.range(0, allDaysBetween)
                    .count() * getOpeningHoursInMinutes(from, to);


            /*Remove any redundant time from opening hours start time from previous records actual start time as long
            as it within the opening hours specified by the rule */
            long trailingRedundantMinutes = 0;
            if (previousRecord.getCreated_at().toLocalTime().isAfter(from.toLocalTime())
                    && previousRecord.getCreated_at().toLocalTime().isBefore(to.toLocalTime())) {
                trailingRedundantMinutes = getOpeningHoursInMinutes(from, previousRecord.getCreated_at());
            }

            /*Remove any redundant time from opening hours end time from current record actual end time
            as long as it within the opening hours specified by the rule */
            long leadingRedundantMinutes = 0;
            if (currentRecord.getCreated_at().toLocalTime().isBefore(to.toLocalTime()) &&
                    currentRecord.getCreated_at().toLocalTime().isAfter(from.toLocalTime())) {
                leadingRedundantMinutes = getOpeningHoursInMinutes(currentRecord.getCreated_at(), to);
            }

            //total time between two previous and current records
            sumOfExpectedUptime += expectedOpeningHoursTimeSpan - trailingRedundantMinutes - leadingRedundantMinutes;

            // If the currentRecord is of uptime, record it
            if (currentRecord.getStatus() == ServiceStatus.OK) {
                //Obtains the time difference
                sumOfActualUptime += sumOfExpectedUptime;
            }

            /* Prepare for next iteration of loop */
            previousRecord = currentRecord;
        }

        return (sumOfActualUptime / sumOfExpectedUptime) * 100; // Uptime percentage calculation
    }

    private static long getOpeningHoursInMinutes(ZonedDateTime from, ZonedDateTime to) {
        LocalDateTime ltdFrom = from.toLocalDateTime();
        LocalDateTime ltdTo = to.toLocalDateTime();
        return ChronoUnit.MINUTES.between(ltdFrom.toLocalTime(), ltdTo.toLocalTime());
    }
}