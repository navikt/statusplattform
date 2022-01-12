package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
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

    public List<ServiceDto> getAllServices2() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retrieveAll();
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
        return EntityDtoMappers.toServiceDtoDeep(service,dependencies);
    }

    public void deleteService(UUID service_id){
        serviceRepository.resetDependenciesOnService(service_id);
        areaRepository.removeServiceFromAllAreas(service_id);

        serviceRepository.delete(service_id);
    }


    public void updateService(ServiceDto serviceDto) {
        serviceRepository.update(EntityDtoMappers.toServiceEntity(serviceDto));
        serviceRepository.removeAllDependenciesFromService(serviceDto.getId());
        serviceRepository.addDependencyToService(EntityDtoMappers.toServiceEntity(serviceDto),
                serviceDto.getDependencies().stream().map(EntityDtoMappers::toServiceEntity)
                        .collect(Collectors.toList()));


    }

    public ServiceDto retrieveOneService(UUID service_id) {
        return EntityDtoMappers.toServiceDtoDeep(serviceRepository.retrieveOneWithDependencies(service_id));
    }
}
