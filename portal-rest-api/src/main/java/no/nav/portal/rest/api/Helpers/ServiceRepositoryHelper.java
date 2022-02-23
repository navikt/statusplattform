package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
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
        result.forEach(serviceDto -> serviceDto.setAreasContainingThisService(getAreasContainingService(serviceDto.getId())));
        //TODO status skal hentes i dbspørringer, ikke slik som dette:
        result.forEach(this::settStatusOnService);
        return result.stream().sorted(Comparator.comparing(ServiceDto::getName)).collect(Collectors.toList());
    }

    private void settStatusOnService(ServiceDto service){
        service.getServiceDependencies().forEach(this::settStatusOnService);
        service.getComponentDependencies().forEach(this::settStatusOnService);
        if(recordRepository.getLatestRecord(service.getId()).isPresent()){
            service.setStatus(StatusDto.fromValue(recordRepository.getLatestRecord(service.getId()).get().getStatus().getDbRepresentation().toUpperCase()));
            return;
        }
        service.setStatus(null);
    }

    public ServiceDto saveNewService(ServiceDto serviceDto){
        ServiceEntity service = EntityDtoMappers.toServiceEntity(serviceDto);
        //Komponenter og tjenester modeleres som forskjellige objektyper i frontend.
        List<ServiceEntity> dependencies = serviceDto.getServiceDependencies()
                .stream().map(EntityDtoMappers::toServiceEntity)
                .collect(Collectors.toList());
        List<ServiceEntity> componentDependencies = serviceDto.getComponentDependencies()
                .stream().map(EntityDtoMappers::toServiceEntity)
                .collect(Collectors.toList());
        dependencies.addAll(componentDependencies);
        UUID uuid = serviceRepository.save(service);
        service.setId(uuid);
        serviceRepository.addDependencyToService(service, dependencies);
        List<UUID> areasCointainingService = serviceDto.getAreasContainingThisService().stream().map(AreaDto::getId).collect(Collectors.toList());
        areaRepository.addServiceToAreas(areasCointainingService,serviceDto.getId());
        return EntityDtoMappers.toServiceDtoDeep(service, dependencies);
    }



    public void deleteService(UUID service_id){
        areaRepository.removeServiceFromAllAreas(service_id);
        //Skal ikke fjerne avhengigheter her.
        serviceRepository.delete(service_id);
    }

    public void deleteComponent(UUID component_id){
        //Skal ikke fjerne avhengigheter her.
        serviceRepository.delete(component_id);
    }

    public void updateService(ServiceDto serviceDto) {
        ServiceEntity serviceEntity = EntityDtoMappers.toServiceEntity(serviceDto);
        serviceRepository.update(serviceEntity)
        ;
        areaRepository.removeServiceFromAllAreas(serviceDto.getId());
        areaRepository
                .addServiceToAreas(serviceDto.getAreasContainingThisService()
                                             .stream().map(AreaDto::getId).collect(Collectors.toList()), serviceDto.getId());


        serviceRepository.removeAllDependenciesFromService(serviceDto.getId());
        List<ServiceDto> dependencies = serviceDto.getComponentDependencies();
        dependencies.addAll(serviceDto.getServiceDependencies());
        serviceRepository.addDependencyToService(serviceEntity,
                dependencies.stream().map(EntityDtoMappers::toServiceEntity)
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

    public MaintenanceDto setMaintenance(MaintenanceDto maintenanceDto) {
        return maintenanceDto;
    }

    public OPSmessageDto addOPSmessega(OPSmessageDto opsMessageDto) {
        return opsMessageDto;
    }
}
