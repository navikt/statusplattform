package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.OpeningHoursEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.AreaRepository;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.fluentjdbc.DbContext;

import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceControllerHelper {
    ServiceRepository serviceRepository;
    RecordRepository recordRepository;
    AreaRepository areaRepository;
    Comparator<ServiceDto> serviceDtoComparator
            = Comparator.comparing(a -> a.getName().toLowerCase());

    public ServiceControllerHelper(DbContext context){
        this.areaRepository = new AreaRepository(context);
        this.serviceRepository = new ServiceRepository(context);
        this.recordRepository = new RecordRepository(context);
    }


    //Denne returnerer avhengigheter ett nivå ned.
    public List<ServiceDto> getAll() {
        Map<ServiceEntity, List<ServiceEntity>> services = serviceRepository.retrieveAllDeep();
        List<ServiceDto> result = services.entrySet().stream().map(EntityDtoMappers::toServiceDtoDeep).toList();
        //TODO status skal hentes i dbspørringer, ikke slik som dette:
        result.forEach(this::settStatusOnService);
        return result.stream().sorted(Comparator.comparing(ServiceDto::getName)).collect(Collectors.toList());
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

        serviceRepository.saveOpeningHours(mapToOpeningHoursEntity(serviceDto.getServiceOpeningHours()));

        //Adding service to areas:
        List<UUID> areasCointainingService = serviceDto.getAreasContainingThisService().stream().map(AreaDto::getId).collect(Collectors.toList());
        areaRepository.addServiceToAreas(areasCointainingService,service.getId());

        ServiceDto result = EntityDtoMappers.toServiceDtoDeep(service, dependencies);
        result.setRecord(new RecordDto());

        return result;
    }


    private static List<OpeningHoursEntity> mapToOpeningHoursEntity(ServiceOpeningHoursDto serviceOpeningHoursDto){
        if (serviceOpeningHoursDto == null){
            return new ArrayList<>();
        }
        return serviceOpeningHoursDto.getDailyOpeningHours()
                .stream()
                .map(ServiceControllerHelper::mapOneDayToOpeningHoursEntity)
                .collect(Collectors.toList());
    }

    private static OpeningHoursEntity mapOneDayToOpeningHoursEntity(ServiceOpeningHoursDayEntryDto serviceOpeningHoursDayEntryDto){
        return new OpeningHoursEntity();
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
}

