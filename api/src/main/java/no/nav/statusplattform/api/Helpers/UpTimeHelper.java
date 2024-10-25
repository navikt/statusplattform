package no.nav.statusplattform.api.Helpers;


import no.nav.statusplattform.generated.api.UpTimeTotalsDto;
import nav.statusplattform.core.openingHours.TimeSpan;
import nav.statusplattform.core.openingHours.UpTimeCalculator;
import nav.statusplattform.core.repositories.OpeningHoursRepository;
import nav.statusplattform.core.repositories.RecordRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import org.fluentjdbc.DbContext;


import java.util.UUID;

public class UpTimeHelper {

    private final UpTimeCalculator upTimeCalculator;


    public UpTimeHelper(DbContext dbContext) {
        this.upTimeCalculator = new UpTimeCalculator(new RecordRepository(dbContext), new OpeningHoursRepository(dbContext));
    }

    public UpTimeTotalsDto getServiceUpTimeSums(UUID service_id, TimeSpan timeSpan) {
        return EntityDtoMappers.toUpTimeTotalsDto(upTimeCalculator.calculateUpTimeForService(service_id, timeSpan));
    }
}
