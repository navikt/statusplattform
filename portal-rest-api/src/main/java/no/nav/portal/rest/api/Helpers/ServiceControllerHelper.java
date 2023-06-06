package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.fluentjdbc.DbContext;

import java.time.Month;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceControllerHelper {
    ServiceRepository serviceRepository;
    RecordRepository recordRepository;
    AreaRepository areaRepository;
    RecordControllerHelper recordControllerHelper;

    Comparator<ServiceDto> serviceDtoComparator
            = Comparator.comparing(a -> a.getName().toLowerCase());

    public ServiceControllerHelper(DbContext context){
        this.areaRepository = new AreaRepository(context);
        this.serviceRepository = new ServiceRepository(context);
        this.recordRepository = new RecordRepository(context);
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
        } else {
            service.setRecord(new RecordDto());
        }
    }

    public ServiceDto saveNewService(ServiceDto serviceDto){
        ServiceEntity service = EntityDtoMappers.toServiceEntity(serviceDto);
        service.setId(serviceRepository.save(service));

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
        return serviceDto;
    }

    public List<ServiceDto> getServicesDependantOnComponent(UUID component_id) {
        return serviceRepository.getServicesDependantOnComponent(component_id).stream().
                map(EntityDtoMappers::toServiceDtoShallow).collect(Collectors.toList());
    }

    public List<AreaDto> getAreasContainingService(UUID service_id) {
        return areaRepository.getAreasContainingService(service_id).stream().
                map(EntityDtoMappers::toAreaDtoShallow).collect(Collectors.toList());
    }

    public MaintenanceDto setMaintenance(MaintenanceDto maintenanceDto) {
        return maintenanceDto;
    }

    public ServiceHistoryDto getServiceHistoryForTwelveMonths(UUID serviceID, int number_of_months) {
        return mapToHistoryDto(recordRepository.getServiceHistoryForNumberOfMonths(serviceID, number_of_months));
    }

    private ServiceHistoryDto mapToHistoryDto(List<DailyStatusAggregationForServiceEntity> aggregationList) {
        ServiceHistoryDto result = new ServiceHistoryDto();
        Arrays.stream(Month.values()).sorted().forEach(month ->
                {
                    List<DailyStatusAggregationForServiceEntity> entriesForTheMonth = aggregationList.stream().filter(listElement ->
                            listElement.getAggregation_date().getMonth().equals(month))
                            .collect(Collectors.toList());

                    if(entriesForTheMonth.size() > 0){
                        ArrayList<ServiceHistoryMonthEntryDto> newEntry = new ArrayList<>();
                        newEntry.add(mapToHistoryMonthDto(entriesForTheMonth, month));
                        newEntry.addAll(result.getHistory());
                        
                        result.setHistory(newEntry);
                    }
                }
        );

        result.getHistory().sort(Comparator.comparing(ServiceHistoryMonthEntryDto::getYear).reversed());
        return result;
    }

    static ServiceHistoryMonthEntryDto mapToHistoryMonthDto(List<DailyStatusAggregationForServiceEntity> listOfDailyStatusOneServiceOneMonth, Month month) {
        ServiceHistoryMonthEntryDto result = new ServiceHistoryMonthEntryDto();
        result.setMonth(Util.mapOfMonthsToNorwegian.get(month));
        result.setEntries(listOfDailyStatusOneServiceOneMonth.stream()
                .map(ServiceControllerHelper::mapToHistoryDayDto)
                .collect(Collectors.toList()));
        result.setYear(result.getEntries().get(0).getDate().getYear());
        return result;
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

