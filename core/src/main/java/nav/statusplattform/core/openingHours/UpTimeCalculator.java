package nav.statusplattform.core.openingHours;


import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;

import nav.statusplattform.core.repositories.OpeningHoursRepository;
import nav.statusplattform.core.repositories.RecordRepository;
import org.fluentjdbc.DbContext;


import javax.swing.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class UpTimeCalculator {

    private final RecordRepository recordRepository;
    private final OpeningHoursRepository openingHoursRepository;

    static UpTimeTotal uptimeTotal;

    public UpTimeCalculator(DbContext context) {

        this.recordRepository = new RecordRepository(context);
        this.openingHoursRepository = new OpeningHoursRepository(context);
    }


    public UpTimeTotal calculateUpTimeForService(UUID serviceId, ZonedDateTime from, ZonedDateTime to) {

        //checks for yyyy-MM-dd
        if (from == null || to == null) {
            throw new IllegalStateException("Arguments for DateEntry must have a format pattern of 'yyyy-MM-dd'");
        }

        //checks if the date entries are greater than the previousRecordCurrentDay date
        if (from.isBefore(ZonedDateTime.now()) && to.isAfter(ZonedDateTime.now())) {
            throw new IllegalStateException("Arguments for DateEntry cannot be greater than the previousRecordCurrentDay date and time");
        }
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

        long totalMinutes = 0;


        //Obtain the first record
        Optional<RecordEntity> firstRecord = records.stream().findFirst();

        if (firstRecord.isEmpty()) {
            throw new NullPointerException(" First record not found for serviceId: " + serviceId);
        }

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
            //Current Record startTime in localTime
            LocalTime recordStartTimeLt = records.get(i).getCreated_at().toLocalTime();
            //Current record StartDateTime in zdt
            ZonedDateTime recordDateTimeZdt = records.get(i).getCreated_at();
            //Opening hours Start Time in zdt
            ZonedDateTime startOfDay = records.get(i).getCreated_at().withHour(ohStart.getHour())
                    .withMinute(ohStart.getMinute());
            //Opening hours End Time in zdt
            ZonedDateTime endOfDay = records.get(i).getCreated_at().withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute());


            //Manage opening hours on a starting day
            if (recordStartTimeLt.isBefore(ohEnd) && recordStartTimeLt.isAfter(ohStart)) {
                totalMinutes += Duration.between(recordDateTimeZdt, endOfDay).toMinutes();
            } else if (recordStartTimeLt.isBefore(ohStart)) {
                totalMinutes += Duration.between(startOfDay, endOfDay).toMinutes();
            }




        }

        //total expected time
        long expectedUptimeTotal = totalMinutes;

        //total actual uptime
        long actualUpTimeTotal = sumOfActualUptime;

        uptimeTotal = new UpTimeTotal(actualUpTimeTotal, expectedUptimeTotal);
    }

    private String getOpeningHours(UUID serviceId, ZonedDateTime zdt) {
        Optional<OpeningHoursGroup> group = openingHoursRepository.getOHGroupForService(serviceId);
        OpeningHoursGroup oHGroupEntity = group.orElseThrow();
        return OpeningHoursParser.getOpeninghours(zdt.toLocalDate(), oHGroupEntity);
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



