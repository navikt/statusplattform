package nav.statusplattform.core.openingHours;


import nav.statusplattform.core.entities.OpeningHoursGroup;

import nav.statusplattform.core.repositories.OpeningHoursRepository;
import nav.statusplattform.core.repositories.RecordRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UpTimeCalculator {

    private final RecordRepository recordRepository;
    private final OpeningHoursRepository openingHoursRepository;

    public UpTimeCalculator(RecordRepository recordRepository, OpeningHoursRepository openingHoursRepository) {
        /* ORLENE: If the data from the repositories is static it is retrieved here to avoid accessing
         the repositories on each call to calculateUpTimeForService.*/
        this.recordRepository = recordRepository;
        this.openingHoursRepository = openingHoursRepository;
    }

    /*Calculation of service's actual and expected uptimes:
     Obtain the opening hours group of business opening times and get the records storing information about a
     service's availability for a period requested by the user. For each record, apply the opening times and create a
     chronology of the service's availability events. Finally, sum up the event times to calculate a service's
     actual and expected uptimes.*/
    public UpTimeTotals calculateUpTimeForService(UUID serviceId, TimeSpan timeSpan) throws IllegalStateException {
        OpeningHoursGroup openingHoursGroup = openingHoursRepository.getOHGroupForService(serviceId)
                .orElseThrow(() -> new IllegalStateException("No timespan assigned for serviceId = %s".formatted(serviceId)));


        Records records = Records.fromRecordEntities(recordRepository.getRecordsInTimeSpan(serviceId, timeSpan.from(), timeSpan.to()), timeSpan);
        List<ActualExpectedUptime> actualExpectedUptimes = records.apply(openingHoursGroup);

        return UpTimeTotals.sumOfAll(actualExpectedUptimes);
    }


}



