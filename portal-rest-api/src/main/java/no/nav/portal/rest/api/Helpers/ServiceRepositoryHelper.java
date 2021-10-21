package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.ServiceDto;
import org.fluentjdbc.DbContext;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceRepositoryHelper {
    ServiceRepository serviceRepository;

    public ServiceRepositoryHelper(DbContext context){
        this.serviceRepository = new ServiceRepository(context);
    }


    //Denne returnerer avhengigheter ett nivå ned.

    public List<ServiceDto> getAllServices2() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retriveAll();
        return services.entrySet().stream().map(EntityDtoMappers::toServiceDtoDeep).collect(Collectors.toList());
    }



    public UUID saveNewService(ServiceDto serviceDto){
        ServiceEntity service = EntityDtoMappers.toServiceEntity(serviceDto);
        List<ServiceEntity> dependencies = serviceDto.getDependencies()
                .stream().map(EntityDtoMappers::toServiceEntity)
                .collect(Collectors.toList());
        UUID uuid = serviceRepository.save(service);
        service.setId(uuid);
        serviceRepository.addDependencyToService(service,dependencies);
        return uuid;
    }

    public void deleteService(UUID service_id){
        serviceRepository.removeAllDependenciesFromService(service_id);
        serviceRepository.delete(service_id);
    }


    public void updateService(ServiceDto serviceDto) {
        serviceRepository.update(EntityDtoMappers.toServiceEntity(serviceDto));
        serviceRepository.addDependencyToService(EntityDtoMappers.toServiceEntity(serviceDto),
                serviceDto.getDependencies().stream().map(EntityDtoMappers::toServiceEntity)
                        .collect(Collectors.toList()));


    }
}
