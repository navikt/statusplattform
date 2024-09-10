package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.openingHours.TimeSpan;
import nav.statusplattform.core.openingHours.UpTimeCalculator;
import nav.statusplattform.core.openingHours.UpTimeTotals;
import nav.statusplattform.core.repositories.OpeningHoursRepository;
import nav.statusplattform.core.repositories.RecordRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import org.fluentjdbc.DbContext;


import java.util.UUID;

public class UpTimeHelper {
    private final RecordRepository recordRepository;
    private final OpeningHoursRepository openingHoursRepository;

    public UpTimeHelper(DbContext dbContext) {
        this.openingHoursRepository = new OpeningHoursRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }


    public UpTimeTotalsDto getServiceUpTimeSums(UUID service_id, TimeSpan timeSpan) {

        UpTimeCalculator upTimeCalculator =
                new UpTimeCalculator(recordRepository, openingHoursRepository);

        UpTimeTotals upTimeTotals = upTimeCalculator.calculateUpTimeForService(service_id, timeSpan);

        return EntityDtoMappers
                .toUpTimeTotalsDto(upTimeTotals);
    }
}
