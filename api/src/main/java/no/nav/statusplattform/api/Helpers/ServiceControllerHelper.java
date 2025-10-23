package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.DailyStatusAggregationForServiceEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.openingHours.OpeningHoursDailyMap;
import nav.statusplattform.core.openingHours.OpeningHoursDisplayData;
import nav.statusplattform.core.openingHours.OpeningHoursParser;
import nav.statusplattform.core.repositories.AreaRepository;
import nav.statusplattform.core.repositories.OpeningHoursRepository;
import nav.statusplattform.core.repositories.RecordRepository;
import nav.statusplattform.core.repositories.ServiceRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.generated.api.AreaDto;
import no.nav.statusplattform.generated.api.MaintenanceDto;
import no.nav.statusplattform.generated.api.OHdisplayDto;
import no.nav.statusplattform.generated.api.RecordDto;
import no.nav.statusplattform.generated.api.ServiceDto;
import no.nav.statusplattform.generated.api.ServiceHistoryDayEntryDto;
import no.nav.statusplattform.generated.api.ServiceHistoryDto;
import no.nav.statusplattform.generated.api.ServiceHistoryMonthEntryDto;
import no.nav.statusplattform.generated.api.StatusDto;
import org.fluentjdbc.DbContext;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServiceControllerHelper {
    ServiceRepository serviceRepository;
    RecordRepository recordRepository;
    AreaRepository areaRepository;
    OpeningHoursRepository openingHoursRepository;
    RecordControllerHelper recordControllerHelper;


    Comparator<ServiceDto> serviceDtoComparator
            = Comparator.comparing(a -> a.getName().toLowerCase());

    public ServiceControllerHelper(DbContext context){
        this.areaRepository = new AreaRepository(context);
        this.serviceRepository = new ServiceRepository(context);
        this.recordRepository = new RecordRepository(context);
        this.openingHoursRepository = new OpeningHoursRepository(context);
        this.recordControllerHelper = new RecordControllerHelper(context);
    }


    //Denne returnerer avhengigheter ett nivå ned.
    public List<ServiceDto> getAll() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retrieveAllDeep();
        List<ServiceDto> result = services.entrySet().stream().map(EntityDtoMappers::toServiceDtoDeep).toList();
        //TODO status skal hentes i dbspørringer, ikke slik som dette:
        result.forEach(this::settStatusOnService);
        return result.stream().sorted(Comparator.comparing(ServiceDto::getName)).collect(Collectors.toList());
    }

    public List<ServiceDto> getAllComponentsShallow() {
        List<ServiceEntity> services = serviceRepository.retrieveAllComponentsShallow();
        List<ServiceDto> result = services.stream().map(EntityDtoMappers::toServiceDtoShallow).toList();
        return result.stream()
                .sorted(serviceDtoComparator)
                .collect(Collectors.toList());
    }

    public List<ServiceDto> getAllComponents() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retrieveAllComponents();
        List<ServiceDto> result = services.entrySet().stream().map(EntityDtoMappers::toServiceDtoDeep).toList();
        result.forEach(componentDto -> componentDto.setServicesDependentOnThisComponent(getServicesDependantOnComponent(componentDto.getId())));
        //TODO status skal hentes i dbspørringer, ikke slik som dette:
        result.forEach(this::settStatusOnService);
        return result.stream()
                .sorted(serviceDtoComparator)
                .collect(Collectors.toList());
    }


    public List<ServiceDto> getAllServicesShallow() {
        List<ServiceEntity> services = serviceRepository.retrieveAllServicesShallow();
        List<ServiceDto> result = services.stream().map(EntityDtoMappers::toServiceDtoShallow).toList();
        return result.stream()
                .sorted(serviceDtoComparator)
                .collect(Collectors.toList());
    }



    public List<ServiceDto> getAllServices() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retrieveAllServices();
        List<ServiceDto> result = services.entrySet().stream().map(EntityDtoMappers::toServiceDtoDeep).toList();
        result.forEach(serviceDto -> serviceDto.setAreasContainingThisService(getAreasContainingService(serviceDto.getId())));
        //TODO status skal hentes i dbspørringer, ikke slik som dette:
        result.forEach(this::settStatusOnService);
        return result.stream()
                .sorted(serviceDtoComparator)
                .collect(Collectors.toList());
    }

    private void settStatusOnService(ServiceDto service){
        service.getServiceDependencies().forEach(this::settStatusOnService);
        service.getComponentDependencies().forEach(this::settStatusOnService);
        if(recordRepository.getLatestRecord(service.getId()).isPresent()){
            service.setRecord(EntityDtoMappers.toRecordDto(recordRepository.getLatestRecord(service.getId()).get()));
        }
        else if(recordRepository.getLatestRecordDiff(service.getId()).isPresent()){
            service.setRecord(EntityDtoMappers.toRecordDtoFromRecordDelta(recordRepository.getLatestRecordDiff(service.getId()).get()));
        }

        else {
            service.setRecord(new RecordDto());
        }
    }

    public ServiceDto saveNewService(ServiceDto serviceDto){
        ServiceEntity service = EntityDtoMappers.toServiceEntity(serviceDto);
        service.setId(serviceRepository.save(service));

        //Adding default opening hours to service open 24/7 all year round
        openingHoursRepository.setDefaultOpeningHoursToService(service.getId());

        //Komponenter og tjenester modeleres som forskjellige objektyper i frontend.
        //Adding dependencies to service:
        List<ServiceEntity> dependencies = serviceDto.getServiceDependencies()
                .stream().map(EntityDtoMappers::toServiceEntity)
                .collect(Collectors.toList());
        List<ServiceEntity> componentDependencies = serviceDto.getComponentDependencies()
                .stream().map(EntityDtoMappers::toServiceEntity)
                .toList();
        dependencies.addAll(componentDependencies);
        serviceRepository.addDependencyToService(service, dependencies);

        //Dersom ny tjeneste er av type komponent, kan andre tjenester være avhengig av denne:
        serviceDto.getServicesDependentOnThisComponent().forEach(s -> serviceRepository.addDependencyToService(s.getId(),service.getId()));


        //Adding service to areas:
        List<UUID> areasCointainingService = serviceDto.getAreasContainingThisService().stream().map(AreaDto::getId).collect(Collectors.toList());
        areaRepository.addServiceToAreas(areasCointainingService,service.getId());

        ServiceDto result = EntityDtoMappers.toServiceDtoDeep(service, dependencies);

        RecordDto recordDto = new RecordDto();
        if(serviceDto.getPollingUrl() == null || serviceDto.getPollingUrl().equals("")){
            recordDto.setServiceId(service.getId());
            recordDto.setStatus(StatusDto.UNKNOWN);
            recordDto.setTimestamp(OffsetDateTime.now());
            recordControllerHelper.updateRecordForService(recordDto);
        }
        result.setRecord(recordDto);

        return result;
    }

    public void updateService(ServiceDto serviceDto) {
        ServiceEntity serviceEntity = EntityDtoMappers.toServiceEntity(serviceDto);
        serviceRepository.update(serviceEntity);
        areaRepository.removeServiceFromAllAreas(serviceDto.getId());
        areaRepository
                .addServiceToAreas(serviceDto
                        .getAreasContainingThisService()
                        .stream().map(AreaDto::getId).collect(Collectors.toList()), serviceDto.getId());
        serviceRepository.resetDependenciesOnService(serviceDto.getId());
        List<ServiceDto> dependencies = serviceDto.getComponentDependencies();
        dependencies.addAll(serviceDto.getServiceDependencies());
        serviceRepository.addDependencyToService(serviceEntity,
                dependencies.stream().map(EntityDtoMappers::toServiceEntity)
                        .collect(Collectors.toList()));
        //Dersom service er av type komponent kan tjenester ha avhengighet til denne
        serviceDto.getServicesDependentOnThisComponent().forEach(s -> serviceRepository.addDependencyToService(s.getId(),serviceDto.getId()));

        if(serviceDto.getPollingUrl() == null || serviceDto.getPollingUrl().equals("")){
            RecordDto recordDto = new RecordDto();
            recordDto.setServiceId(serviceDto.getId());
            recordDto.setTimestamp(OffsetDateTime.now());
            recordDto.setStatus(StatusDto.UNKNOWN);
            recordControllerHelper.updateRecordForService(recordDto);
        }


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



    public ServiceDto retrieveOneService(UUID service_id) {
        ServiceDto serviceDto =  EntityDtoMappers.toServiceDtoDeep(serviceRepository.retrieveOneWithDependencies(service_id));
        settStatusOnService(serviceDto);
        setOHdisplayOnService(serviceDto);
        return serviceDto;
    }

    private static void setOHdisplayOnService(ServiceDto serviceDto) {
        Map<UUID, OpeningHoursDisplayData>todaysDisplayData  = OpeningHoursDailyMap.getMap();
        OpeningHoursDisplayData ohdd = todaysDisplayData.getOrDefault(serviceDto.getId(), null);
        if(ohdd != null){
            boolean isOpen = OpeningHoursParser.isOpen(LocalDateTime.now(), ohdd.getRule());
            String displayText = ohdd.getOpeningHours();
            displayText = !isOpen? "Stengt " + displayText: displayText;
            serviceDto.setOhDisplay(
                    new OHdisplayDto()
                            .openingHours(ohdd.getOpeningHours())
                            .rule(ohdd.getRule())
                            .displayText(displayText)
                            .isOpen(isOpen));

        }
    }



    public List<ServiceDto> getServicesDependantOnComponent(UUID component_id) {
        return serviceRepository.getServicesDependantOnComponent(component_id).stream().
                map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList());
    }

    public List<AreaDto> getAreasContainingService(UUID service_id) {
        return areaRepository.getAreasContainingService(service_id).stream().
                map(EntityDtoMappers::toAreaDtoShallow).collect(Collectors.toList());
    }

    static ServiceHistoryDayEntryDto mapToHistoryDayDto(DailyStatusAggregationForServiceEntity entity){
        ServiceHistoryDayEntryDto result = new ServiceHistoryDayEntryDto();
        result.information(entity.getInformation());
        result.serviceId(entity.getService_id());
        result.setStatus(getAggregationStatus(entity));
        result.setDate(entity.getAggregation_date());
        return result;
    }


    static StatusDto getAggregationStatus(DailyStatusAggregationForServiceEntity aggregation){
        return aggregation.getNumber_of_status_down()>0 ? StatusDto.DOWN:
                (aggregation.getNumber_of_status_issue()>0 ? StatusDto.ISSUE : StatusDto.OK);
        }

    public List<ServiceDto> getPollingServices() {
        List<ServiceEntity> services = serviceRepository.retrieveServicesWithPollingGcp();
        //If polling is onprem, updated status is on statusholder
        services.forEach(s -> {if(s.getPollingOnPrem()) s.setPolling_url("STATUSHOLDER");});
        services = services.stream().filter(s -> s.getPolling_url() != null && !s.getPolling_url().equals("")).collect(Collectors.toList());
        return services.stream().map(EntityDtoMappers::toServiceDtoShallow).toList();
    }

    public List<ServiceDto> retrieveServicesWithPollingOnPrem() {
        List<ServiceEntity> services = serviceRepository.retrieveServicesWithPollingOnPrem();
        services = services.stream().filter(s -> s.getPolling_url() != null && !s.getPolling_url().equals("")).collect(Collectors.toList());
        return services.stream().map(EntityDtoMappers::toServiceDtoShallow).toList();
    }
}
