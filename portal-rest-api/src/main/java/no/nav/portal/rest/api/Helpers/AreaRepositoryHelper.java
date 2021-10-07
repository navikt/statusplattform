package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.AreaDto;
import org.actioncontroller.HttpForbiddenException;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.stream.Collectors;

public class AreaRepositoryHelper {
    private final AreaRepository areaRepository;
    private final DashboardRepository dashboardRepository;
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;


    public AreaRepositoryHelper(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }



    public AreaDto newArea(AreaDto areaDto){
        try{
            areaRepository.retrieve(areaDto.getId());
            throw new HttpForbiddenException("Område med id: "+ areaDto.getId() +" finnes fra før.");
        }
        catch (IllegalArgumentException e){
            areaRepository.save(EntityDtoMappers.toEntity(areaDto));
            return EntityDtoMappers.toDto(areaRepository.retrieve(areaDto.getId()));
        }

    }


    public List<AreaDto> getAreasOnDashboard(String dashboardName){
        DashboardDto dashboardDto  =  EntityDtoMappers.toDto(dashboardRepository.retrieve(dashboardName));
        List<String> areaCodes = dashboardDto.getAreasIds();
        return areaRepository.retrieve(areaCodes).stream()
                .sorted(Comparator.comparing(AreaEntity::getRangering))
                .map(EntityDtoMappers::toDto)
                .collect(Collectors.toList());
    }





}
