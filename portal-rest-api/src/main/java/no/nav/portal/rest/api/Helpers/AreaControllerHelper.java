package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.DashboardRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.SubAreaDto;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class AreaControllerHelper {
    private final AreaRepository areaRepository;
    private final DashboardRepository dashboardRepository;
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;


    public AreaControllerHelper(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }


    public List<AreaDto> getAllAreas(){
        List<AreaDto> result = new ArrayList<>();
        Map<AreaEntity,List<ServiceEntity>> areaAndServises  = areaRepository.retrieveAll();
        areaAndServises.entrySet().forEach(entry -> {
            AreaDto areaDto =  EntityDtoMappers.toAreaDtoDeep(entry.getKey(),entry.getValue());
            areaDto.setSubAreas(areaRepository.getSubAreasOnArea(entry.getKey().getId())
                    .stream()
                    .map(EntityDtoMappers::toSubAreaDtoShallow)
                    .collect(Collectors.toList()));
            result.add(areaDto);
                }
        );
        Comparator<AreaDto> areaNameComparator
                = Comparator.comparing(a -> a.getName().toLowerCase());
        return result.stream().sorted(areaNameComparator)
                .collect(Collectors.toList());
    }


    public AreaDto newArea(AreaDto areaDto){
        UUID uuid = areaRepository.save(EntityDtoMappers.toAreaEntity(areaDto));
        Map.Entry<AreaEntity, List<ServiceEntity>> area = areaRepository.retrieveOne(uuid);
        return EntityDtoMappers.toAreaDtoDeep(area.getKey(), area.getValue());
    }

    public AreaDto updateArea(UUID areaId, AreaDto areaDto){
        //update services
        areaRepository.setServicesOnArea(areaId,areaDto.getServices().stream()
                .map(ServiceDto::getId).collect(Collectors.toList()));
        areaRepository.addSubAreaToArea(areaId,areaDto.getSubAreas().stream()
                .map(SubAreaDto::getId).collect(Collectors.toList()));

        //update sub areas
        areaDto.setId((areaId));
        areaRepository.updateArea(EntityDtoMappers.toAreaEntity(areaDto));
        Map.Entry<AreaEntity,List<ServiceEntity>> area = areaRepository.retrieveOne(areaId);

        return EntityDtoMappers.toAreaDtoDeep(area.getKey(),area.getValue());
    }


    public List<AreaDto> getAreasOnDashboard(UUID dashboardName_id){
        return dashboardRepository.retrieveOne(dashboardName_id)
                .getValue()
                .stream()
                .map(as -> EntityDtoMappers
                        .toAreaDtoDeep(as.getArea(),as.getServices()))
                .collect(Collectors.toList());
    }


    public List<AreaDto> getAreasOnDashboard(String dashboardName){
        UUID dashboardUid = dashboardRepository.uidFromName(dashboardName);
        return getAreasOnDashboard(dashboardUid);
    }





}
