package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.StatusDto;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceRepositoryHelper {
    ServiceRepository serviceRepository;
    RecordRepository recordRepository;
    AreaRepository areaRepository;

    public ServiceRepositoryHelper(DbContext context){
        this.areaRepository = new AreaRepository(context);
        this.serviceRepository = new ServiceRepository(context);
        this.recordRepository = new RecordRepository(context);
    }


    //Denne returnerer avhengigheter ett nivå ned.
    public List<ServiceDto> getAll() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retrieveAll();
        List<ServiceDto> result = services.entrySet().stream().map(EntityDtoMappers::toServiceDtoDeep).collect(Collectors.toList());
        //TODO status skal hentes i dbspørringer, ikke slik som dette:
        result.forEach(this::settStatusOnService);
        return result.stream().sorted(Comparator.comparing(ServiceDto::getName)).collect(Collectors.toList());
    }
    public List<ServiceDto> getAllComponents() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retrieveAllComponents();
        List<ServiceDto> result = services.entrySet().stream().map(EntityDtoMappers::toServiceDtoDeep).collect(Collectors.toList());
        //TODO status skal hentes i dbspørringer, ikke slik som dette:
        result.forEach(this::settStatusOnService);
        return result.stream().sorted(Comparator.comparing(ServiceDto::getName)).collect(Collectors.toList());
    }

    public List<ServiceDto> getAllServices() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retrieveAllServices();
        List<ServiceDto> result = services.entrySet().stream().map(EntityDtoMappers::toServiceDtoDeep).collect(Collectors.toList());
        //TODO status skal hentes i dbspørringer, ikke slik som dette:
        result.forEach(this::settStatusOnService);
        return result.stream().sorted(Comparator.comparing(ServiceDto::getName)).collect(Collectors.toList());
    }

    private void settStatusOnService(ServiceDto service){
        service.getDependencies().forEach(this::settStatusOnService);
        if(recordRepository.getLatestRecord(service.getId()).isPresent()){
            service.setStatus(StatusDto.fromValue(recordRepository.getLatestRecord(service.getId()).get().getStatus().getDbRepresentation().toUpperCase()));
            return;
        }
        service.setStatus(null);
    }

    public ServiceDto saveNewService(ServiceDto serviceDto){
        ServiceEntity service = EntityDtoMappers.toServiceEntity(serviceDto);
        List<ServiceEntity> dependencies = serviceDto.getDependencies()
                .stream().map(EntityDtoMappers::toServiceEntity)
                .collect(Collectors.toList());
        UUID uuid = serviceRepository.save(service);
        service.setId(uuid);
        serviceRepository.addDependencyToService(service,dependencies);
        areaRepository.addServiceToAreas(serviceDto.getAreasContainingThisService(),serviceDto.getId());
        return EntityDtoMappers.toServiceDtoDeep(service,dependencies);
    }

    public void deleteService(UUID service_id){
        areaRepository.removeServiceFromAllAreas(service_id);
        //Skal ikke fjerne avhengigheter her.
        serviceRepository.delete(service_id);
    }

    public void updateService(ServiceDto serviceDto) {
        ServiceEntity serviceEntity = EntityDtoMappers.toServiceEntity(serviceDto);
        serviceRepository.update(serviceEntity);
        serviceRepository.removeAllDependenciesFromService(serviceDto.getId());
        serviceRepository.addDependencyToService(serviceEntity,
                serviceDto.getDependencies().stream().map(EntityDtoMappers::toServiceEntity)
                        .collect(Collectors.toList()));


    }

    public ServiceDto retrieveOneService(UUID service_id) {
        ServiceDto serviceDto =  EntityDtoMappers.toServiceDtoDeep(serviceRepository.retrieveOneWithDependencies(service_id));
        settStatusOnService(serviceDto);
        return serviceDto;
    }

    public List<AreaDto> getAreasContainingService(UUID service_id) {
        return areaRepository.getAreasContainingService(service_id).stream().
                map(EntityDtoMappers::toAreaDtoShallow).collect(Collectors.toList());
    }
}
