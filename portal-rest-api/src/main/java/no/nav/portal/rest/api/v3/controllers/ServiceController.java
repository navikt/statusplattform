package no.nav.portal.rest.api.v3.controllers;


import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.Helpers.ServiceRepositoryHelper;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.ServiceTypeDto;
import org.actioncontroller.DELETE;
import org.actioncontroller.GET;
import org.actioncontroller.POST;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.Arrays;
import java.util.List;
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
    public List<ServiceDto> getServices() {
        return serviceRepositoryHelper.getAllServices();
    }

    @POST("/Service")
    @JsonBody
    public void newService(@JsonBody ServiceDto serviceDto) {
        serviceRepositoryHelper.saveNewService(serviceDto);
    }

    @DELETE("/Service")
    @JsonBody
    public void deleteService(@JsonBody ServiceDto serviceDto) {
        serviceRepositoryHelper.deleteService(serviceDto);
    }

    @GET("/Services/Typer")
    @JsonBody
    public List<String> getServicetyper() {
        return Arrays.stream(ServiceTypeDto.values()).map(ServiceTypeDto::toString).collect(Collectors.toList());
    }
}
