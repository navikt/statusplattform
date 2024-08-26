package nav.statusplattform.core.openingHours;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

record DayDuringTimeline(LocalDate actualDay) {

    DailyUptime dailyUptimeFrom(Records records) {
        List<ServiceDown> serviceDowns = records.intervals()
                .stream()
                .filter(record -> record.isValidFor(actualDay))
                .filter(RecordInterval::isDown)
                .map(record -> ServiceDown.from(record, actualDay))
                .collect(toList());

        return new DailyUptime(actualDay, serviceDowns);
    }
}