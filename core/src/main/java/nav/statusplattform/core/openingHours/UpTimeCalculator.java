package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;

import nav.statusplattform.core.repositories.OpeningHoursRepository;
import nav.statusplattform.core.repositories.RecordRepository;
import org.fluentjdbc.DbContext;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class UpTimeCalculator {

    private final RecordRepository recordRepository;
    private final OpeningHoursRepository openingHoursRepository;

    static UpTimeTotal uptimeTotal;

    public UpTimeCalculator(DbContext context) {

        this.recordRepository = new RecordRepository(context);
        this.openingHoursRepository = new OpeningHoursRepository(context);
    }


    public UpTimeTotal calculateUpTimeForService(UUID serviceId, ZonedDateTime from, ZonedDateTime to) throws IllegalStateException {

        //Throws an exception if the to and from period is of  yyyy-MM-dd - HH:mm:ss format
        if (from == null || to == null) {
            throw new IllegalStateException("Arguments for DateEntry must consist of a date and time of 'yyyy-MM-dd - HH:mm:ss");
        }

        //Throws an exception if the date entries are greater than the previousRecordCurrentDay date
        if (from.isBefore(ZonedDateTime.now()) && to.isAfter(ZonedDateTime.now())) {
            throw new IllegalStateException("Arguments for DateEntry cannot be greater than the previousRecordCurrentDay date and time");
        }
        calculatePercentageUptime(serviceId, from, to);

        return uptimeTotal.getUptimeTotals();
    }

    private void calculatePercentageUptime(UUID serviceId, ZonedDateTime from, ZonedDateTime to) {
        // Records sorted in chronological order
        List<RecordEntity> records;

        records = recordRepository.getRecordsInTimeSpan(serviceId, from, to);
        if (records.isEmpty()) {
            throw new NullPointerException("Records not found for serviceId: " + serviceId);
        }

        long sumOfActualUptime = 0L; //total actual uptime
        long sumOfExpectedUptime = 0L; //total expected time

        //Obtain the first record
        Optional<RecordEntity> firstRecord = records.stream().findFirst();

        /*Exclude the period of time starting before the requested start date time from the first record as
         this is not to be included as part of the calculation. */
        if (firstRecord.get().getCreated_at().isBefore(from)) {
            firstRecord.get().setCreated_at(from);
        }

        //get the Services opening hours start and end times from the data entry start date
        LocalTime ohStart = getOpeningHoursStart(serviceId, from);
        LocalTime ohEnd = getOpeningHoursEnd(serviceId, from);

        //Sum up (A) all the time  service has been UP, and all the time service should have been up
        for (int i = 0; i < records.size() - 1; i++) {
            //Current Record start date Time in localTime
            LocalTime currentCreatedAtLt = records.get(i).getCreated_at().toLocalTime();
            //Next Record startDate and Time in localTime
            LocalTime nextCreatedAtLt = records.get(i + 1).getCreated_at().toLocalTime();
            //Current record StartDateTime in zdt
            ZonedDateTime currentCreatedAtZdt = records.get(i).getCreated_at();
            //Next record StartDateTime in zdt
            ZonedDateTime nextCreatedAtZdt = records.get(i + 1).getCreated_at();
            //Opening hours Start Time in zdt
            ZonedDateTime startOfDay = records.get(i).getCreated_at().withHour(ohStart.getHour())
                    .withMinute(ohStart.getMinute());
            //Opening hours End Time in zdt
            ZonedDateTime endOfDay = records.get(i).getCreated_at().withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute());

            //Check if the current record is of uptime; if so, sum it up to actual uptime.
            boolean isValidUptime = records.get(i).getStatus().equals(ServiceStatus.OK);

            //Manage opening hours on the starting day
            if ((zonedDateTimeDifference(currentCreatedAtZdt, nextCreatedAtZdt) > 1)) {
                if (currentCreatedAtLt.isBefore(ohEnd) && currentCreatedAtLt.isAfter(ohStart)) {
                    sumOfExpectedUptime += Duration.between(currentCreatedAtZdt, endOfDay).toMinutes();
                } else if (currentCreatedAtLt.isBefore(ohStart)) {
                    sumOfExpectedUptime += Duration.between(startOfDay, endOfDay).toMinutes();
                }
            }

            //Sum the duration of time that goes over a period of days
            //Get the opening hours start time
            ohStart = getOpeningHoursStart(serviceId, currentCreatedAtZdt.truncatedTo(ChronoUnit.DAYS).plusDays(1));
            ohEnd = getOpeningHoursEnd(serviceId, currentCreatedAtZdt.truncatedTo(ChronoUnit.DAYS).plusDays(1));
            //Set it on the current date
            currentCreatedAtZdt = currentCreatedAtZdt.truncatedTo(ChronoUnit.DAYS).plusDays(1).withHour(ohStart.getHour()).withMinute(ohStart.getMinute());

            // Iterate and calculate the duration of time for full days
            while (currentCreatedAtZdt.isBefore(nextCreatedAtZdt.truncatedTo(ChronoUnit.DAYS))) {
                endOfDay = currentCreatedAtZdt.withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute());
                sumOfExpectedUptime += Duration.between(currentCreatedAtZdt, endOfDay).toMinutes();


                //get the next day and its corresponding opening hours start and end times
                ohStart = getOpeningHoursStart(serviceId, currentCreatedAtZdt.plusDays(1));
                ohEnd = getOpeningHoursEnd(serviceId, currentCreatedAtZdt.plusDays(1));
                currentCreatedAtZdt = currentCreatedAtZdt.plusDays(1).withHour(ohStart.getHour()).withMinute(ohStart.getMinute());
            }

            //partial day on the ending day
            //obtain opening hours for date
            ohStart = getOpeningHoursStart(serviceId, nextCreatedAtZdt);
            ohEnd = getOpeningHoursEnd(serviceId, nextCreatedAtZdt);
            if (nextCreatedAtLt.isBefore(ohEnd) && nextCreatedAtLt.isAfter(ohStart)) {
                startOfDay = nextCreatedAtZdt.withHour(ohStart.getHour()).withMinute(ohStart.getMinute());
                sumOfExpectedUptime += Duration.between(startOfDay, nextCreatedAtZdt).toMinutes();
            } else if (nextCreatedAtLt.isAfter(ohEnd)) {
                //add the duration of the last date with its respective opening and ending hours
                sumOfExpectedUptime += Duration
                        .between(nextCreatedAtZdt.withHour(ohStart.getHour()).withMinute(ohStart.getMinute()),
                                nextCreatedAtZdt.withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute())).toMinutes();
            }

            if (isValidUptime) {
                sumOfActualUptime += sumOfExpectedUptime;
            }
        }

        //Last record
        RecordEntity lastRecord = records.getLast();
        boolean isValidUptime = lastRecord.getStatus().equals(ServiceStatus.OK);

        //get the Services opening hours start and end times from the data entry start date
        ohStart = getOpeningHoursStart(serviceId, lastRecord.getCreated_at());
        ohEnd = getOpeningHoursEnd(serviceId, lastRecord.getCreated_at());

        //Last Record startTime in localTime
        LocalTime lastRecordCreatedAtLt = lastRecord.getCreated_at().toLocalTime();
        //Last record StartDateTime in zdt
        ZonedDateTime lastRecordCreatedAtZdt = lastRecord.getCreated_at();
        //Opening hours Start Time in zdt
        ZonedDateTime startOfDay = lastRecord.getCreated_at().withHour(ohStart.getHour())
                .withMinute(ohStart.getMinute());
        //Opening hours End Time in zdt
        ZonedDateTime endOfDay = lastRecord.getCreated_at().withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute());

        if ((zonedDateTimeDifference(lastRecord.getCreated_at(), to) > 1)) {
            // Handle partial day on the starting day
            if (lastRecordCreatedAtLt.isBefore(ohEnd) && lastRecordCreatedAtLt.isAfter(ohStart)) {
                sumOfExpectedUptime += Duration.between(lastRecordCreatedAtZdt, endOfDay).toMinutes();
            } else if (lastRecordCreatedAtLt.isBefore(ohStart)) {
                sumOfExpectedUptime += Duration.between(startOfDay, endOfDay).toMinutes();
            }
        }

        //Sum the duration of time that goes over a period of days
        //Get the opening hours start time
        ohStart = getOpeningHoursStart(serviceId, lastRecordCreatedAtZdt.truncatedTo(ChronoUnit.DAYS).plusDays(1));
        ohEnd = getOpeningHoursEnd(serviceId, lastRecordCreatedAtZdt.truncatedTo(ChronoUnit.DAYS).plusDays(1));
        //Set it on the current date
        ZonedDateTime currentDay = lastRecordCreatedAtZdt.truncatedTo(ChronoUnit.DAYS).plusDays(1).withHour(ohStart.getHour()).withMinute(ohStart.getMinute());

        // Iterate and calculate the duration of time for full days
        while (currentDay.isBefore(to.truncatedTo(ChronoUnit.DAYS))) {
            endOfDay = currentDay.withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute());
            sumOfExpectedUptime += Duration.between(currentDay, endOfDay).toMinutes();

            //get the next day and its corresponding opening hours start and end times
            ohStart = getOpeningHoursStart(serviceId, currentDay.plusDays(1));
            ohEnd = getOpeningHoursEnd(serviceId, currentDay.plusDays(1));
            currentDay = currentDay.plusDays(1).withHour(ohStart.getHour()).withMinute(ohStart.getMinute());
        }

        //partial day on the ending day
        //obtain opening hours for date
        ohStart = getOpeningHoursStart(serviceId, to);
        ohEnd = getOpeningHoursEnd(serviceId, to);
        if (to.toLocalTime().isBefore(ohEnd) && to.toLocalTime().isAfter(ohStart)) {
            startOfDay = to.withHour(ohStart.getHour()).withMinute(ohStart.getMinute());
            sumOfExpectedUptime += Duration.between(startOfDay, to).toMinutes();
        } else if (to.toLocalTime().isAfter(ohEnd)) {
            //add the duration of the last date with its respective opening and ending hours
            sumOfExpectedUptime += Duration.between(to.withHour(ohStart.getHour()).withMinute(ohStart.getMinute()), to.withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute())).toMinutes();
        }

        if (isValidUptime) {
            sumOfActualUptime += sumOfExpectedUptime;
        }

        //Actual and Expected Uptime totals
        uptimeTotal = new UpTimeTotal(sumOfActualUptime, sumOfExpectedUptime);
    }

    //get the Services opening hours start and end times from the data entry start date
    private LocalTime getOpeningHoursStart(UUID serviceId, ZonedDateTime zdt) {
        Optional<OpeningHoursGroup> group = openingHoursRepository.getOHGroupForService(serviceId);
        OpeningHoursGroup oHGroupEntity = group.orElseThrow();
        String ohString = OpeningHoursParser.getOpeninghours(zdt.toLocalDate(), oHGroupEntity);
        return OpeningHoursParser.getOpeningTime(ohString);
    }

    private LocalTime getOpeningHoursEnd(UUID serviceId, ZonedDateTime zdt) {
        Optional<OpeningHoursGroup> group = openingHoursRepository.getOHGroupForService(serviceId);
        OpeningHoursGroup oHGroupEntity = group.orElseThrow();
        String ohString = OpeningHoursParser.getOpeninghours(zdt.toLocalDate(), oHGroupEntity);
        return OpeningHoursParser.getClosingTime(ohString);
    }

    static long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2) {
        return ChronoUnit.DAYS.between(d1, d2);
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



