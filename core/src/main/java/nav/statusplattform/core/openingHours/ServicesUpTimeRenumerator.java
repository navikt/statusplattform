package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.RecordRepository;
import org.fluentjdbc.DbContext;


import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.LocalDate.*;

public class ServicesUpTimeRenumerator {
    RecordRepository recordRepository;
    static UptimeTotals uptimeTotals;

    public ServicesUpTimeRenumerator(DbContext context) {
        this.recordRepository = new RecordRepository(context);
    }

    public UptimeTotals calculateUpTimeForService(UUID serviceId, LocalDate DateEntryFrom, LocalDate DateEntryTo, String rule) {
        String[] ruleParts = rule.split("\s");

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
            uptimeTotals = new UptimeTotals(0, 0);
            return uptimeTotals.getUptimeTotals();
        }

        //Application is always up
        if (openingHours.equals("00:00-23:59")) {
            openingHours = "00:00-23:59";
        }

        String[] openingHoursParts = openingHours.split("-");

        //Obtain the time in hh:mm format
        String[] ohStartTime = openingHoursParts[0].split(":"); //start time hours[0] and minutes[1]
        String[] ohEndTime = openingHoursParts[1].split(":"); //end time hours[0] and minutes[1]

        //Convert string to local date format to get  LocalDateFormat that can be used to obtain zone datetime
        LocalTime oHStartTime = LocalTime.of(Integer.parseInt(ohStartTime[0]), Integer.parseInt(ohStartTime[1]));
        LocalTime oHEndTime = LocalTime.of(Integer.parseInt(ohEndTime[0]), Integer.parseInt(ohEndTime[1]));

        //Obtained zonedDateTime
        ZonedDateTime from = ZonedDateTime.of(DateEntryFrom, oHStartTime, ZoneId.of("Europe/Oslo"));
        ZonedDateTime to = ZonedDateTime.of(DateEntryTo, oHEndTime, ZoneId.of("Europe/Oslo"));

        calculatePercentageUptime(serviceId, from, to);

        return uptimeTotals.getUptimeTotals();
    }

    public void calculatePercentageUptime(UUID serviceId, ZonedDateTime from, ZonedDateTime to) {
        // Records  sorted in chronological order
        List<RecordEntity> records;

        //If no records are found
        try {
            records = recordRepository.getRecordsInTimeSpan(serviceId, from, to);
        } catch (Exception e) {
            throw new RuntimeException("No records not found for serviceId: " + serviceId);
        }

        long sumOfActualUptime = 0L; //the total time the service is up
        long sumOfExpectedUptime = 0L; //total sum of time duration
        long totalOHMinutesBetweenPeriods; //total sum of time duration between each record

        Optional<RecordEntity> firstRecord = records.stream()
                .filter(Objects::nonNull)
                .findFirst();

        if (firstRecord.isEmpty()) {
            throw new NullPointerException(" First record not found for serviceId: " + serviceId);
        }
        RecordEntity previousRecord = firstRecord.get();


        //Sum up (A) all the time  service has been UP, and all the time service should have been up
        for (RecordEntity currentRecord : records.subList(1, records.size())) {

            // Sum the total number of time in days between the two periods
            int noOfDays = (int) noOfDaysBetweenTwoPeriods(previousRecord.getCreated_at(), currentRecord.getCreated_at());

            /*if no Of days is zero indicating a period less than a day, (under 24 hours) set nofDays to 1 as
            it forms part of the calculation for determining number of work hours*/

            if (noOfDays == 0) {
                noOfDays = 1;
            }

            //Obtains the total of Opening hours start and end time in minutes
            totalOHMinutesBetweenPeriods = ChronoUnit.MINUTES.between(from.toLocalTime(), to.toLocalTime()) * noOfDays;

            //calculate
            long excessStartMinutes = 0;
            if (withinWorkingHours(previousRecord, from, to)) {
                excessStartMinutes = ChronoUnit.MINUTES.between(from.toLocalTime(), previousRecord.getCreated_at().toLocalTime());
            }

            long excessEndMinutes = 0;
            if (withinWorkingHours(currentRecord, from, to)) {
                excessEndMinutes = ChronoUnit.MINUTES.between(currentRecord.getCreated_at().toLocalTime(), to.toLocalTime());
            }


            //Calculate uptime duration
            //uptime duration before down time
            if ((previousRecord.getStatus() == ServiceStatus.OK &&
                    currentRecord.getStatus() == ServiceStatus.DOWN) ||

                    //uptime duration before an issue
                    (previousRecord.getStatus() == ServiceStatus.OK &&
                            currentRecord.getStatus() == ServiceStatus.ISSUE) ||

                    //uptime duration before an unknown event
                    (previousRecord.getStatus() == ServiceStatus.OK &&
                            currentRecord.getStatus() == ServiceStatus.UNKNOWN)) {

                sumOfActualUptime += totalOHMinutesBetweenPeriods - excessStartMinutes - excessEndMinutes;
            }

            sumOfExpectedUptime += totalOHMinutesBetweenPeriods - excessStartMinutes - excessEndMinutes;

            /* Prepare for next iteration of loop */
            previousRecord = currentRecord;
        }

        //todo manage last record

        RecordEntity lastRecord = records.getLast();


        //todo
        //total expected time
        long expectedUptimeTotal = sumOfExpectedUptime;
        //todo
        //total actual up time
        long actualUpTimeTotal = sumOfActualUptime;

        uptimeTotals = new UptimeTotals(actualUpTimeTotal, expectedUptimeTotal);
    }

    private long noOfDaysBetweenTwoPeriods(ZonedDateTime periodOne, ZonedDateTime periodTwo) {
        // Returns the sum of Opening Hours times between the records previous and current times.
        return (int) (ChronoUnit.DAYS.between(periodOne, periodTwo));
    }

    //Check that time falls within working hours
    private boolean withinWorkingHours(RecordEntity recordEntity, ZonedDateTime from, ZonedDateTime to) {
        // Has a value of  0 or less if  opening hours start time is before or equal to the record start time
        int withinStartTime = from.toLocalTime().compareTo(recordEntity.getCreated_at().toLocalTime());

        // Has a value greater than zero if opening hours end time is equal or after the record end time
        int withinEndTime = to.toLocalTime().compareTo(recordEntity.getCreated_at().toLocalTime());

        //Returns true if if record is within opening hours, otherwise false.
        return withinStartTime <= 0 && withinEndTime >= 0;
    }

}

final class UptimeTotals {
    private final long sumOfActualUptime;
    private final long sumOfExpectedUptime;

    public UptimeTotals(long sumOfActualUptime, long sumOfExpectedUptime) {
        this.sumOfActualUptime = sumOfActualUptime;
        this.sumOfExpectedUptime = sumOfExpectedUptime;
    }

    public long getSumOfActualUptime() {
        return sumOfActualUptime;
    }

    public long getSumOfExpectedUptime() {
        return sumOfExpectedUptime;
    }

    public UptimeTotals getUptimeTotals() {
        return new UptimeTotals(getSumOfActualUptime(), getSumOfExpectedUptime());
    }
}

