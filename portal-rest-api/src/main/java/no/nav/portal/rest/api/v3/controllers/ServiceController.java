package no.nav.portal.rest.api.v3.controllers;


import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.ServiceControllerHelper;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class ServiceController {

    private final ServiceControllerHelper serviceControllerHelper;
    private final ServiceRepository serviceRepository;


    public ServiceController(DbContext dbContext) {
        this.serviceControllerHelper = new ServiceControllerHelper(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
    }

    @GET("/Services")
    @JsonBody
    public  List<ServiceDto> getServices() {
        return serviceControllerHelper.getAllServices();
    }

    @GET("/Components")
    @JsonBody
    public  List<ServiceDto> getComponents() {
        return serviceControllerHelper.getAllComponents();
    }


    @DELETE("/Component/:Service_id")
    @JsonBody
    public void deleteComponent(@PathParam("Service_id") UUID componentId) {
        serviceControllerHelper.deleteComponent(componentId);
    }

    @GET("/Service/:Service_id")
    @JsonBody
    public ServiceDto getService(@PathParam("Service_id") UUID service_id) {
        return serviceControllerHelper.retrieveOneService(service_id);
    }

    @GET("/Service/HistoryAggregated/:Service_id")
    @JsonBody
    public ServiceHistoryDto getServiceHistoryTwelveMonthsBack(@PathParam("Service_id") UUID service_id) {
        return serviceControllerHelper.getServiceHistoryForTwelveMonths(service_id, 12);
    }

    @POST("/Service")
    @JsonBody
    public ServiceDto newService(@JsonBody ServiceDto serviceDto) {
        return serviceControllerHelper.saveNewService(serviceDto);
    }

    @PUT("/Service/:Service_id")
    @JsonBody
    public void updateService(@PathParam("Service_id") UUID service_id, @JsonBody ServiceDto serviceDto) {
        serviceDto.setId(service_id);
        serviceControllerHelper.updateService(serviceDto);
    }

    @PUT("/Service/:Service_id/:DependentOnService_id")
    @JsonBody
    public void addDependencyToService(@PathParam("Service_id") UUID service_id
            ,@PathParam("DependentOnService_id") UUID dependentOnService_id) {
        serviceRepository.addDependencyToService(service_id,dependentOnService_id);
    }

    @DELETE("/Service/:Service_id/:DependentOnService_id")
    public void removeDependencyFromService(@PathParam("Service_id") UUID service_id
            ,@PathParam("DependentOnService_id") UUID dependentOnService_id) {
        serviceRepository.removeDependencyFromService(service_id,dependentOnService_id);
    }



    @DELETE("/Service/:Service_id")
    @JsonBody
    public void deleteService(@PathParam("Service_id") UUID service_id) {
        serviceControllerHelper.deleteService(service_id);
    }


    @PUT("/Service/Maintenance")
    @JsonBody
    public void addMaintenance(@JsonBody MaintenanceDto maintenanceDto) {
        serviceRepository.saveMaintenance(EntityDtoMappers.toMaintenanceEntity(maintenanceDto));
    }

    @GET("/Service/Maintenance/:Service_id")
    @JsonBody
    public List<MaintenanceDto> addMaintenance(@PathParam("Service_id") UUID service_id) {
        return serviceRepository.getMaintenanceForService(service_id).stream().map(EntityDtoMappers::toMaintenanceDto).collect(Collectors.toList());
    }


    @GET("/Service/Areas/:Service_id")
    @JsonBody
    public List<AreaDto> getAreasContainingService(@PathParam("Service_id") UUID service_id) {

        return serviceControllerHelper.getAreasContainingService(service_id);
    }


    @GET("/Services/Types")
    @JsonBody
    public List<String> getServicetypes() {
        return ServiceTypeDto.getValues();
    }

    @GET("/Services/Status")
    @JsonBody
    public List<String> getServiceStatuses() {
        return StatusDto.getValues();
    }

}
