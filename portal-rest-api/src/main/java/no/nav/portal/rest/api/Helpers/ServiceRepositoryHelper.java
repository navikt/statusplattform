package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.ServiceDto;
import org.fluentjdbc.DbContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceRepositoryHelper {
    ServiceRepository serviceRepository;

    public ServiceRepositoryHelper(DbContext context){
        this.serviceRepository = new ServiceRepository(context);
    }


    //Denne returnerer avhengigheter ett nivå ned.
    public List<ServiceDto> getAllServices() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retriveAll();
        List<ServiceDto> result = services.keySet()
                .stream().map(EntityDtoMappers::toServiceDto)
                .collect(Collectors.toList());
        result.forEach(s -> {
            List<ServiceEntity> dependencies = services.get(s);
            dependencies = dependencies != null ? dependencies : Collections.EMPTY_LIST;
            s.dependencies(dependencies.stream()
                    .map(EntityDtoMappers::toServiceDto).collect(Collectors.toList()));
            });
        return result;

    }

    public void saveNewService(ServiceDto serviceDto){
        ServiceEntity service = EntityDtoMappers.toServiceEntity(serviceDto);
        List<ServiceEntity> dependencies = serviceDto.getDependencies()
                .stream().map(EntityDtoMappers::toServiceEntity)
                .collect(Collectors.toList());
        serviceRepository.save(service);
        serviceRepository.addDependenciesToService(service,dependencies);
    }

    public void deleteService(ServiceDto serviceDto){
        if(serviceRepository.isOtherServicesDependentOn(serviceDto.getId())){
            throw new IllegalArgumentException("Kan ikke slette tjeneste," +
                    " da andre tjenester er avhengig av denne. Disse avhengighetene må fjernes først");
        }
        serviceRepository.delete(serviceDto.getId());
    }


}
