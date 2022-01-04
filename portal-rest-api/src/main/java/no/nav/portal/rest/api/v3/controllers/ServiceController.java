package no.nav.portal.rest.api.v3.controllers;


import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.infrastructure.PortalRestPrincipal;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.ServiceRepositoryHelper;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.ServiceStatusDto;
import no.portal.web.generated.api.ServiceTypeDto;
import no.portal.web.generated.api.StatusDto;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class ServiceController {

    private final ServiceRepositoryHelper serviceRepositoryHelper;
    private final ServiceRepository serviceRepository;


    public ServiceController(DbContext dbContext) {
        this.serviceRepositoryHelper = new ServiceRepositoryHelper(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
    }

    @GET("/Services")
    @JsonBody
    public  List<ServiceDto> getServices() {
        return serviceRepositoryHelper.getAllServices2();
    }

    @GET("/Service/:Service_id")
    @JsonBody
    public ServiceDto getService(@PathParam("Service_id") UUID service_id) {
        return serviceRepositoryHelper.retrieveOneService(service_id);
    }

    @POST("/Service")
    @JsonBody
    public ServiceDto newService(@JsonBody ServiceDto serviceDto) {
        return serviceRepositoryHelper.saveNewService(serviceDto);
    }
    @PUT("/Service/:Service_id")
    @JsonBody
    public void updateService(@PathParam("Service_id") UUID service_id, @JsonBody ServiceDto serviceDto) {
        serviceRepositoryHelper.updateService(serviceDto);
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
        serviceRepositoryHelper.deleteService(service_id);
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
