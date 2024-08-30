package nav.statusplattform.core.openingHours;


import nav.statusplattform.core.entities.OpeningHoursGroup;

import nav.statusplattform.core.repositories.OpeningHoursRepository;
import nav.statusplattform.core.repositories.RecordRepository;

import java.util.List;
import java.util.UUID;

public class UpTimeCalculator {

    private final RecordRepository recordRepository;
    private final OpeningHoursRepository openingHoursRepository;

    public UpTimeCalculator(RecordRepository recordRepository, OpeningHoursRepository openingHoursRepository) {
        this.recordRepository = recordRepository;
        this.openingHoursRepository = openingHoursRepository;
    }

    public UpTimeTotal calculateUpTimeForService(UUID serviceId, TimeSpan timeSpan) throws IllegalStateException {
        OpeningHoursGroup openingHoursGroup = openingHoursRepository.getOHGroupForService(serviceId)
                .orElseThrow(() -> new IllegalStateException("Didn´t find any opening hours for serviceId = %s".formatted(serviceId)));


        Records records = Records.fromRecordEntities(recordRepository.getRecordsInTimeSpan(serviceId, timeSpan.from(), timeSpan.to()), timeSpan);
        List<ActualExpectedUptime> actualExpectedUptimes = records.apply(openingHoursGroup);

        return UpTimeTotal.sumOfAll(actualExpectedUptimes);
    }

}



