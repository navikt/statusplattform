package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.enums.ServiceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record Records(List<RecordEntity> records) {

    List<RecordInterval> toRecordInterval() {

        if (records.isEmpty()) {
            throw new IllegalStateException("There has to be at least one record in the list.");
        }

        RecordsBuilder recordsBuilder = RecordsBuilder.init(records.get(0));

        for (int i=1; i < records.size(); i++) {
            RecordEntity currentRecord = records.get(i);
            recordsBuilder.append(currentRecord);
        }

        return recordsBuilder.build();
    }
}

class RecordsBuilder {

    private ServiceStatus status;
    private LocalDateTime localDateTime;

    private List<RecordInterval> intervals = new ArrayList<>();

    static RecordsBuilder init(RecordEntity recordEntity) {
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

    List<RecordInterval> build() {
        RecordInterval recordInterval = new RecordInterval(localDateTime, null, status);
        this.intervals.add(recordInterval);
        return intervals;
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

