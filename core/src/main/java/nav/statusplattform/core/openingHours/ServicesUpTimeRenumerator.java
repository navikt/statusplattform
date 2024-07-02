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
    static UpTimeTotal uptimeTotal;

    public ServicesUpTimeRenumerator(DbContext context) {
        this.recordRepository = new RecordRepository(context);
    }

    public UpTimeTotal calculateUpTimeForService(UUID serviceId, LocalDate DateEntryFrom, LocalDate DateEntryTo, String rule) {
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
            uptimeTotal = new UpTimeTotal(0, 0);
            return uptimeTotal.getUptimeTotals();
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

        return uptimeTotal.getUptimeTotals();
    }

    public void calculatePercentageUptime(UUID serviceId, ZonedDateTime from, ZonedDateTime to) {
        // Records sorted in chronological order
        List<RecordEntity> records;

        //Throws an exception if no records are found
        try {
            records = recordRepository.getRecordsInTimeSpan(serviceId, from, to);
        } catch (Exception e) {
            throw new RuntimeException("Records not found for serviceId: " + serviceId);
        }

        long sumOfActualUptime = 0L; //total time service is up (in minutes)
        long sumOfExpectedUptime = 0L; //total sum of entire time duration
        long totalOHMinutesBetweenPeriods; //total time duration sum between each record


        //Obtain the first record
        Optional<RecordEntity> firstRecord = records.stream()
                .filter(Objects::nonNull)
                .findFirst();

        if (firstRecord.isEmpty()) {
            throw new NullPointerException(" First record not found for serviceId: " + serviceId);
        }
        RecordEntity previousRecord = firstRecord.get();


        //Sum up (A) all the time  service has been UP, and all the time service should have been up
        for (RecordEntity currentRecord : records.subList(1, records.size())) {

            // Sum the total number of time between each record
            /*if no Of days is zero indicating a period less than a day, (under 24 hours) set nofDays to 1 as
            it forms part of the calculation for determining number of work hours*/
            int noOfDays = (int) noOfDaysBetweenTwoPeriods(previousRecord.getCreated_at(), currentRecord.getCreated_at()) + 1;

            //Obtains the total minutes of all the time service should have been up for number of days between previous
            // and last current record
            totalOHMinutesBetweenPeriods = ChronoUnit.MINUTES.between(from.toLocalTime(), to.toLocalTime()) * noOfDays;

            /*Calculates any time difference from opening hours start time and the previous record created time
            if the previous record created time falls within opening hours but starts after the opening hours start time.
            This time difference is not included as part of the calculation*/
            long excessStartMinutes = 0;
            if (withinWorkingHours(previousRecord, from, to)) {
                excessStartMinutes = ChronoUnit.MINUTES.between(from.toLocalTime(), previousRecord.getCreated_at().toLocalTime());
            }

            /*Calculates any time difference from opening hours end time and the current record created time
            if the current record created time falls within opening hours but ends before the opening hours end time.
            This time difference is not included as part of the calculation*/
            long excessEndMinutes = 0;
            if (withinWorkingHours(currentRecord, from, to)) {
                excessEndMinutes = ChronoUnit.MINUTES.between(currentRecord.getCreated_at().toLocalTime(), to.toLocalTime());
            }


            //Calculate the service's actual uptime (service status = Ok) between previous and current record
            if ((previousRecord.getStatus() == ServiceStatus.OK &&
                    currentRecord.getStatus() == ServiceStatus.DOWN) ||

                    //uptime duration before another Ok
                    (previousRecord.getStatus() == ServiceStatus.OK &&
                            currentRecord.getStatus() == ServiceStatus.OK) ||

                    //uptime duration before an issue
                    (previousRecord.getStatus() == ServiceStatus.OK &&
                            currentRecord.getStatus() == ServiceStatus.ISSUE) ||

                    //uptime duration before an unknown event
                    (previousRecord.getStatus() == ServiceStatus.OK &&
                            currentRecord.getStatus() == ServiceStatus.UNKNOWN)) {

                /*Services actual uptime total number of minutes between previous record and current record
                created times*/
                sumOfActualUptime += totalOHMinutesBetweenPeriods - excessStartMinutes - excessEndMinutes;
            }

            /*Services actual uptime total number of minutes between previous record and current record
             created times*/
            sumOfExpectedUptime += totalOHMinutesBetweenPeriods - excessStartMinutes - excessEndMinutes;

            /* Prepare for next iteration of loop */
            previousRecord = currentRecord;
        }

        //Obtain the number of minutes from the last record
        RecordEntity lastRecord = records.getLast();

        /* Sum the total number of time in days between the two periods.  Add 1 if the period between
        last record and end date is under 1 as the number of days is used in the calculation.
        */

        long totalMinsBetweenLastRecordAndTo = 0;

        ZonedDateTime currentTime = ZonedDateTime.now();
        int noOfDays = (int) noOfDaysBetweenTwoPeriods(lastRecord.getCreated_at(), to);


        //The total of Opening hours start and end time in minutes
        if (noOfDays > 1) {
            totalMinsBetweenLastRecordAndTo = ChronoUnit.MINUTES.between(lastRecord.getCreated_at().toLocalTime(), to.toLocalTime()) * noOfDays;
        }


        /*Calculates minutes from the last record start time if its time is greater than the opening hours start time */
        long minsToLastRecordActualStart = 0;

        if (withinWorkingHours(lastRecord, from, to)) {
            if (currentTime.toLocalTime().isBefore(to.toLocalTime())) {
                minsToLastRecordActualStart = ChronoUnit.MINUTES.between(lastRecord.getCreated_at().toLocalTime(), currentTime.toLocalTime());
            } else {
                minsToLastRecordActualStart = ChronoUnit.MINUTES.between(lastRecord.getCreated_at().toLocalTime(), to.toLocalTime());
            }
        }


        //Last record total minutes
        long expectedUptimeLastRecord = totalMinsBetweenLastRecordAndTo - minsToLastRecordActualStart;
        //long expectedUptimeLastRecord = minsFromStartToLastRecordActualStart;

        //if the last record is up, its time is included in the calculation
        long actualUpTimeLastRecord = 0L;
        if (lastRecord.getStatus() == ServiceStatus.OK) {
            actualUpTimeLastRecord = expectedUptimeLastRecord;
        }

        //total expected time
        long expectedUptimeTotal = sumOfExpectedUptime + expectedUptimeLastRecord;

        //total actual up time
        long actualUpTimeTotal = sumOfActualUptime + actualUpTimeLastRecord;

        uptimeTotal = new UpTimeTotal(actualUpTimeTotal, expectedUptimeTotal);
    }

    private long noOfDaysBetweenTwoPeriods(ZonedDateTime periodOne, ZonedDateTime periodTwo) {
        // Returns the sum of Opening Hours times between the records previous and current times.
        return (int) (ChronoUnit.DAYS.between(periodOne, periodTwo));
    }

    /*Returns true if the time record is created is greater than the opening hours start time and is less than the end times*/
    private boolean withinWorkingHours(RecordEntity recordEntity, ZonedDateTime from, ZonedDateTime to) {
        // Has a value of  0 or less if opening hours start time is before or equal to the record start time
        int withinStartTime = from.toLocalTime().compareTo(recordEntity.getCreated_at().toLocalTime());

        // Has a value greater than zero if opening hours end time is equal or after the record end time
        int withinEndTime = to.toLocalTime().compareTo(recordEntity.getCreated_at().toLocalTime());

        //Returns true if record is within opening hours, otherwise false.
        return withinStartTime <= 0 && withinEndTime >= 0;
    }
}

//Returns the actual and expected uptimes
final class UpTimeTotal {
    private final long sumOfActualUptime;
    private final long sumOfExpectedUptime;

    public UpTimeTotal(long sumOfActualUptime, long sumOfExpectedUptime) {
        this.sumOfActualUptime = sumOfActualUptime;
        this.sumOfExpectedUptime = sumOfExpectedUptime;
    }

    public long getSumOfActualUptime() {
        return sumOfActualUptime;
    }

    public long getSumOfExpectedUptime() {
        return sumOfExpectedUptime;
    }

    public UpTimeTotal getUptimeTotals() {
        return new UpTimeTotal(getSumOfActualUptime(), getSumOfExpectedUptime());
    }
}

