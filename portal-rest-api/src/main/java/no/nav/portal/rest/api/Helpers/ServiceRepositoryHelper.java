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
    //TODO fjern ubrukt kode under
    /**
    public List<ServiceDto> getAllServices() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retriveAll();
        Map<ServiceDto,Map.Entry<ServiceEntity, List<ServiceEntity>>> dtoEntityMap = new HashMap<>();
        List<ServiceDto> result = new ArrayList<>();
        services.entrySet().forEach(
                entry -> {
                            ServiceDto dto = EntityDtoMappers.toServiceDtoShallow(entry.getKey());
                            dtoEntityMap.put(dto, entry);
                            result.add(dto);
                        }
                );
        dtoEntityMap.entrySet().forEach(entry -> {
            List<ServiceEntity> dependencies = entry.getValue().getValue();
            dependencies = dependencies != null ? dependencies : Collections.EMPTY_LIST;
            entry.getKey().setDependencies(dependencies.stream()
                    .map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList()));
            });
        return result;
    }
     **/
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
        serviceRepository.addDependenciesToService(service,dependencies);
        return uuid;
    }

    public void deleteService(UUID service_id){
        if(serviceRepository.isOtherServicesDependentOn(service_id)){
            throw new IllegalArgumentException("Kan ikke slette tjeneste," +
                    " da andre tjenester er avhengig av denne. Disse avhengighetene må fjernes først");
        }
        serviceRepository.delete(service_id);
    }


    public void updateService(ServiceDto serviceDto) {
        serviceRepository.update(EntityDtoMappers.toServiceEntity(serviceDto));
        serviceRepository.addDependenciesToService(EntityDtoMappers.toServiceEntity(serviceDto),
                serviceDto.getDependencies().stream().map(EntityDtoMappers::toServiceEntity)
                        .collect(Collectors.toList()));


    }
}
