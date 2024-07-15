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
        Optional<RecordEntity> firstRecord = records.stream()
                .filter(Objects::nonNull)
                .findFirst();

        if (firstRecord.isEmpty()) {
            throw new NullPointerException(" First record not found for serviceId: " + serviceId);
        }
        RecordEntity previousRecord = firstRecord.get();

        //get the Services opening hours start and end times from the data entry start date
        String ohString = getOpeningHours(serviceId, from);
        LocalTime ohStart = OpeningHoursParser.getOpeningTime(ohString);
        LocalTime ohEnd = OpeningHoursParser.getOpeningTime(ohString);

        previousRecord.setCreated_at(from);

        //Sum up (A) all the time  service has been UP, and all the time service should have been up
        for (int i = 0; i < records.size() - 1; i++) {

            //todo update comment
            //get records.get() opening hours start and end for full days  - used in calculation for full days
            String ohStringNextDay = getOpeningHours(serviceId, records.get(i).getCreated_at().plusDays(1));
            LocalTime ohStartNextDay = OpeningHoursParser.getOpeningTime(ohStringNextDay);
            LocalTime ohEndNextDay = OpeningHoursParser.getOpeningTime(ohStringNextDay);

            ZonedDateTime current = records.get(i).getCreated_at().plusDays(1)
                    .withHour(ohStartNextDay.getHour()).withMinute(ohStartNextDay.getMinute());

            // Handle partial day on the starting day

            if (records.get(i).getCreated_at().toLocalTime().isBefore(ohEnd) && records.get(i).getCreated_at().toLocalTime().isAfter(ohStart)) {
                ZonedDateTime endOfDay = records.get(i).getCreated_at().withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute());
                totalMinutes += Math.min(Duration.between(records.get(i).getCreated_at(), endOfDay)
                        .toMinutes(), Duration.between(records.get(i).getCreated_at(), records.get(i + 1).getCreated_at()).toMinutes());
            } else if (records.get(i).getCreated_at().toLocalTime().isBefore(ohStart)) {
                totalMinutes += Duration.between(records.get(i).getCreated_at().withHour(ohStart.getHour())
                        .withMinute(ohStart.getMinute()), records.get(i).getCreated_at()
                        .withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute())).toMinutes();
            }

            //todo update comments
            // Handle any full days between current and next record
            while (current.isBefore(records.get(i + 1).getCreated_at())) {
                ZonedDateTime endOfDay = current.withHour(ohEndNextDay.getHour()).withMinute(ohEndNextDay.getMinute());
                totalMinutes += Duration.between(current, endOfDay).toMinutes();
                //obtain opening hours for date
                ohStringNextDay = getOpeningHours(serviceId, records.get(i).getCreated_at().plusDays(1));
                ohStartNextDay = OpeningHoursParser.getOpeningTime(ohStringNextDay);
                ohEndNextDay = OpeningHoursParser.getOpeningTime(ohStringNextDay);
                current = current.plusDays(1).withHour(ohStartNextDay.getHour()).withMinute(ohStartNextDay.getMinute());
            }

            //todo update comment
            if (records.get(i + 1).getCreated_at().toLocalTime()
                    .isBefore(ohEndNextDay) && records.get(i + 1).getCreated_at().toLocalTime().isAfter(ohStartNextDay)) {
                totalMinutes += Duration.between(current, records.get(i + 1).getCreated_at()).toMinutes();
            } else if (records.get(i + 1).getCreated_at().toLocalTime().isAfter(ohEndNextDay)) {
                totalMinutes += Duration.between(current.withHour(ohStartNextDay.getHour()).withMinute(ohStartNextDay.getMinute()),
                        records.get(i + 1).getCreated_at().withHour(ohEndNextDay.getHour()).withMinute(ohEndNextDay.getMinute())).toMinutes();
            }

            //Last record
            RecordEntity lastRecord = records.getLast();
            //get the Services opening hours start and end times from the data entry start date
            ohString = getOpeningHours(serviceId, lastRecord.getCreated_at());
            ohStart = OpeningHoursParser.getOpeningTime(ohString);
            ohEnd = OpeningHoursParser.getOpeningTime(ohString);

            // Handle partial day on the starting day
            if (lastRecord.getCreated_at().toLocalTime().isBefore(ohEnd) && lastRecord.getCreated_at().toLocalTime().isAfter(ohStart)) {
                ZonedDateTime endOfDay = lastRecord.getCreated_at().withHour(ohEnd.getHour()).withMinute(ohEnd
                        .getMinute());
                totalMinutes += Math.min(Duration.between(lastRecord.getCreated_at(), endOfDay).toMinutes(), Duration.between(lastRecord.getCreated_at(), to).toMinutes());
            } else if (lastRecord.getCreated_at().toLocalTime().isBefore(ohStart)) {
                totalMinutes += Duration.between(lastRecord.getCreated_at().withHour(ohStart.getHour()).withMinute(ohStart.getMinute()), lastRecord.getCreated_at().withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute())).toMinutes();
            }


            //todo
            //Last record next day
            ZonedDateTime lastRecordNextDay = records.getLast().getCreated_at().truncatedTo(ChronoUnit.DAYS).plusDays(1).withHour(ohStart.getHour()).withMinute(ohStart.getMinute());

            //get opening hours start and end times for next day
            String ohLastRecordString = getOpeningHours(serviceId, records.getLast().getCreated_at().truncatedTo(ChronoUnit.DAYS).plusDays(1));
            LocalTime ohLastRecordNextDayStart = OpeningHoursParser.getOpeningTime(ohLastRecordString);
            LocalTime ohLastRecordNextDayEnd = OpeningHoursParser.getOpeningTime(ohLastRecordString);

            // Handle full days between start and end
            while (lastRecordNextDay.isBefore(to.truncatedTo(ChronoUnit.DAYS))) {
                ZonedDateTime endOfDay = lastRecordNextDay.withHour(ohLastRecordNextDayEnd.getHour()).withMinute(ohLastRecordNextDayEnd.getMinute());
                totalMinutes += Duration.between(lastRecordNextDay, endOfDay).toMinutes();
                current = current.plusDays(1).withHour(ohLastRecordNextDayStart.getHour()).withMinute(ohLastRecordNextDayStart.getMinute());
            }

            // Handle partial day on the ending day
            //obtain opening hours for date
            ohString = getOpeningHours(serviceId, to);
            ohStart = OpeningHoursParser.getOpeningTime(ohStringNextDay);
            ohEnd = OpeningHoursParser.getOpeningTime(ohStringNextDay);
            if (to.toLocalTime().isBefore(ohEnd) && to.toLocalTime().isAfter(ohStart)) {
                ZonedDateTime startOfDay = to.withHour(ohStart.getHour()).withMinute(ohStart.getMinute());
                totalMinutes += Duration.between(startOfDay, to).toMinutes();
            } else if (to.toLocalTime().isAfter(ohEnd)) {
                totalMinutes += Duration.between(to.withHour(ohStart.getHour()).withMinute(ohStart.getMinute()), to.withHour(ohEnd.getHour()).withMinute(ohEnd.getMinute())).toMinutes();
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



