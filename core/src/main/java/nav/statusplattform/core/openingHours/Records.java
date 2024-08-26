package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

record Records(List<RecordInterval> intervals, TimeSpan timeSpan) {

    Records {
        if (intervals == null || intervals.isEmpty()) {
            throw new IllegalStateException("The list of RecordInterval can´t be empty.");
        }
    }

    static Records fromRecordEntities(List<RecordEntity> records, TimeSpan timeSpan) {

        if (records.isEmpty()) {
            throw new IllegalStateException("There has to be at least one record in the list.");
        }

        RecordsBuilder recordsBuilder = new RecordsBuilder().init(records.get(0));

        for (int i=1; i < records.size(); i++) {
            RecordEntity currentRecord = records.get(i);
            recordsBuilder.append(currentRecord);
        }

        return new Records(recordsBuilder.build(timeSpan.to()), timeSpan);
    }

    public List<ActualExpectedUptime> apply(OpeningHoursGroup openingHoursGroup) {
        return timeSpan.allDays().stream()
                .map(actualDay -> dailyUptimeFrom(actualDay))
                .map(dailyUptime -> dailyUptime.apply(openingHoursGroup))
                .toList();
    }

    private DailyUptime dailyUptimeFrom(LocalDate actualDay) {
        List<ServiceDown> serviceDowns = this.intervals
                .stream()
                .filter(record -> record.isValidFor(actualDay))
                .filter(RecordInterval::isDown)
                .map(record -> ServiceDown.from(record, actualDay))
                .collect(toList());

        return new DailyUptime(actualDay, serviceDowns);
    }

    private static class RecordsBuilder {

        private ServiceStatus status;
        private LocalDateTime localDateTime;

        private List<RecordInterval> intervals = new ArrayList<>();

        RecordsBuilder init(RecordEntity recordEntity) {
            RecordsBuilder recordsBuilder = new RecordsBuilder();
            recordsBuilder.localDateTime = recordEntity.getCreated_at().toLocalDateTime();
            recordsBuilder.status = recordEntity.getStatus();
            return recordsBuilder;
        }

        void append(RecordEntity currentRecord) {
            RecordInterval recordInterval = new RecordInterval(localDateTime, currentRecord.getCreated_at().toLocalDateTime(), status);
            this.intervals.add(recordInterval);
            this.localDateTime = currentRecord.getCreated_at().toLocalDateTime();
            this.status = currentRecord.getStatus();
        }

        List<RecordInterval> build(LocalDateTime endOfTimeSpan) {
            RecordInterval recordInterval = new RecordInterval(this.localDateTime, endOfTimeSpan, status);
            this.intervals.add(recordInterval);
            return intervals;
        }
    }
}

record RecordInterval(LocalDateTime from, LocalDateTime to, ServiceStatus serviceStatus) {
    boolean isValidFor(LocalDate actualDay) {
        return (actualDay.isEqual(from.toLocalDate()) || actualDay.isAfter(from.toLocalDate())
                && (to == null || (actualDay.isEqual(to.toLocalDate()) || actualDay.isBefore(to.toLocalDate()))));
    }

    boolean isDown() {
        return serviceStatus != ServiceStatus.OK;
    }
}