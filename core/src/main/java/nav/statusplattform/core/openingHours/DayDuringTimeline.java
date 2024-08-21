package nav.statusplattform.core.openingHours;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

public record DayDuringTimeline(LocalDate actualDay) {

    public DailyUptime dailyUptimeFrom(Records records) {
        List<RecordInterval> recordInterval = records.toRecordInterval();

        List<ServiceDown> serviceDowns = recordInterval.stream()
                .filter(record -> record.isValidFor(actualDay))
                .filter(RecordInterval::isDown)
                .map(record -> ServiceDown.from(record, actualDay))
                .collect(toList());

        return new DailyUptime(actualDay, serviceDowns);
    }
}