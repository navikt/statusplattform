package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.entities.SubAreaEntity;
import nav.statusplattform.core.repositories.AreaRepository;
import nav.statusplattform.core.repositories.DashboardRepository;
import nav.statusplattform.core.repositories.RecordRepository;
import nav.statusplattform.core.repositories.ServiceRepository;
import nav.statusplattform.core.repositories.SubAreaRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.generated.api.SubAreaDto;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SubAreaControllerHelper {
    private final AreaRepository areaRepository;
    private final SubAreaRepository subAreaRepository;
    private final DashboardRepository dashboardRepository;
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;


    public SubAreaControllerHelper(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.subAreaRepository = new SubAreaRepository(dbContext);
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }

    public SubAreaDto newSubArea(SubAreaDto subAreaDto){
        UUID uuid = subAreaRepository.save(EntityDtoMappers.toSubAreaEntity(subAreaDto));
        Map.Entry<SubAreaEntity, List<ServiceEntity>> subArea = subAreaRepository.retrieveOne(uuid);
        return EntityDtoMappers.toSubAreaDtoDeep(subArea.getKey(), subArea.getValue());
    }
}
