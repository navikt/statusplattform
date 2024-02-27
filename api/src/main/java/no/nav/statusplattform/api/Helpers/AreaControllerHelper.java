package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.AreaEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.repositories.AreaRepository;
import nav.statusplattform.core.repositories.DashboardRepository;
import nav.statusplattform.core.repositories.RecordRepository;
import nav.statusplattform.core.repositories.ServiceRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.generated.api.AreaDto;
import no.nav.statusplattform.generated.api.ServiceDto;
import no.nav.statusplattform.generated.api.ServiceTypeDto;
import no.nav.statusplattform.generated.api.SubAreaDto;
import org.actioncontroller.HttpRequestException;
import org.fluentjdbc.DbContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AreaControllerHelper {
    private final AreaRepository areaRepository;
    private final DashboardRepository dashboardRepository;
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;
    Comparator<AreaDto> areaNameComparator
            = Comparator.comparing(a -> a.getName().toLowerCase());

    public AreaControllerHelper(DbContext dbContext) {
        this.areaRepository = new AreaRepository(dbContext);
        this.dashboardRepository = new DashboardRepository(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
    }


    public List<AreaDto> getAllAreasShallow(){
        List<AreaDto> result = areaRepository.retriveAllShallow()
                .stream().map(EntityDtoMappers::toAreaDtoShallow)
                .collect(Collectors.toList());
        return result.stream().sorted(areaNameComparator)
                .collect(Collectors.toList());
    }

    public List<AreaDto> getAllAreasWithComponentsShallow(){
        List<AreaDto> result = areaRepository.retriveAllWithComponentsShallow()
                .stream().map(EntityDtoMappers::toAreaDtoShallow)
                .collect(Collectors.toList());
        return result.stream().sorted(areaNameComparator)
                .collect(Collectors.toList());
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

        return result.stream().sorted(areaNameComparator)
                .collect(Collectors.toList());
    }


    public AreaDto newArea(AreaDto areaDto){
        boolean containsComponent = areaDto.getServices().stream().anyMatch(s -> s.getType().equals(ServiceTypeDto.KOMPONENT));
        if((areaDto.getContainsComponents() == null  || !areaDto.getContainsComponents())&& containsComponent){
            throw new HttpRequestException("Kan ikke legge til komponent i område som ikke skal ha komponenter ");
        }
        UUID uuid = areaRepository.save(EntityDtoMappers.toAreaEntity(areaDto));


        areaRepository.setServicesOnArea(uuid,
                areaDto.getServices().stream().map(
                        ServiceDto::getId).collect(Collectors.toList()));

        Map.Entry<AreaEntity, List<ServiceEntity>> area = areaRepository.retrieveOne(uuid);
        return EntityDtoMappers.toAreaDtoDeep(area.getKey(), area.getValue());
    }

    public AreaDto updateArea(AreaDto areaDto){
        boolean containsComponent = areaDto.getServices().stream().anyMatch(s -> s.getType().equals(ServiceTypeDto.KOMPONENT));
        if((areaDto.getContainsComponents() == null  || !areaDto.getContainsComponents()) && containsComponent){
            throw new HttpRequestException("Kan ikke legge til komponent i område som ikke skal ha komponenter ");
        }
        //update services
        areaRepository.setServicesOnArea(areaDto.getId(),areaDto.getServices().stream()
                .map(ServiceDto::getId).collect(Collectors.toList()));
        //update sub areas
        areaRepository.addSubAreaToArea(areaDto.getId(),areaDto.getSubAreas().stream()
                .map(SubAreaDto::getId).collect(Collectors.toList()));

        areaRepository.updateArea(EntityDtoMappers.toAreaEntity(areaDto));
        Map.Entry<AreaEntity,List<ServiceEntity>> area = areaRepository.retrieveOne(areaDto.getId());

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
