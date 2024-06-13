package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.RecordRepository;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.LocalDate.*;

public class ServicesUpTimeRenumerator {
    static RecordRepository recordRepository;

    public static long calculateUpTimeForService(UUID serviceId, LocalDate DateEntryFrom, LocalDate DateEntryTo, String rule) {
        String[] ruleParts = rule.split("[\s]");

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
        LocalTime ohLocalTimeStart = LocalTime.of(Integer.parseInt(ohStartTime[0]), Integer.parseInt(ohStartTime[1]));
        LocalTime ohLocalTimeEnd = LocalTime.of(Integer.parseInt(ohEndTime[0]), Integer.parseInt(ohEndTime[1]));


        //Obtained zonedDateTime
        ZonedDateTime from = ZonedDateTime.of(DateEntryFrom, ohLocalTimeStart, (ZoneId.of("Europe/Oslo")));
        ZonedDateTime to = ZonedDateTime.of(DateEntryTo, ohLocalTimeEnd, (ZoneId.of("Europe/Oslo")));

        return calculatePercentageUptime(serviceId, from, to);

    }

    public static long calculatePercentageUptime(UUID serviceId, ZonedDateTime from, ZonedDateTime to) {
        // These records Have to be sorted in chronological order
        List<RecordEntity> records = recordRepository.getRecordsInTimeSpan(serviceId, from, to);

        /*if (records.isEmpty()){
            throw new IllegalStateException("The service must contain records recording it's status.");
        }*/

        long sumOfActualUptime = 0;
        long sumOfExpectedUptime = 0;
        RecordEntity previousRecord = records.getFirst();
        ZonedDateTime previousTimestamp = previousRecord.getCreated_at();

        //Sum up (A) all the time  service has been UP, and all the time service should have been up
        for (RecordEntity currentRecord : records.subList(1, records.size())) {
            // Get the summarized (NOT AVERAGED) amount of minutes of uptime in this timespan that the openinghours rule(s) expect
            //Obtains the time difference between two periods
            long expectedOpeningHoursTimeSpan = getTimeDifferenceInMinutes(previousTimestamp, currentRecord.getCreated_at());
            sumOfExpectedUptime += expectedOpeningHoursTimeSpan;

            // If the currentRecord is of uptime, record it
            if (currentRecord.getStatus() == ServiceStatus.OK) {
                //Obtains the time difference
                sumOfActualUptime += getTimeDifferenceInMinutes(previousRecord.getCreated_at(), currentRecord.getCreated_at());
            }
            // Prepare for next iteration of loop
            previousRecord = currentRecord;
        }

        RecordEntity lastRecord = records.getLast();
        if (lastRecord.getCreated_at().isBefore(to)) {
            //Obtains the time difference between two periods
            long expectedOpeningHoursTimeSpan = getTimeDifferenceInMinutes(lastRecord.getCreated_at(), to);
            sumOfExpectedUptime += expectedOpeningHoursTimeSpan;

            //If the current record is of uptime , record it
            if (lastRecord.getStatus() == ServiceStatus.OK) {
                //Obtains the time difference
                sumOfActualUptime += getTimeDifferenceInMinutes(lastRecord.getCreated_at(), to);
            }

        }
        return (sumOfActualUptime / sumOfExpectedUptime) * 100; // Uptime percentage calculation
    }

    static long getTimeDifferenceInMinutes(ZonedDateTime d1, ZonedDateTime d2) {
        return ChronoUnit.MINUTES.between(d1, d2);
    }


}
