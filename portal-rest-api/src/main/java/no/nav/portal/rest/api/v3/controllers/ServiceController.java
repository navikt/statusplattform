package no.nav.portal.rest.api.v3.controllers;


import no.nav.portal.rest.api.Helpers.ServiceRepositoryHelper;
import no.portal.web.generated.api.ServiceDto;
import org.actioncontroller.DELETE;
import org.actioncontroller.GET;
import org.actioncontroller.POST;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;


public class ServiceController {

    private final ServiceRepositoryHelper serviceRepositoryHelper;


    public ServiceController(DbContext dbContext) {
        this.serviceRepositoryHelper = new ServiceRepositoryHelper(dbContext);
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

    @GET("/Services")
    @JsonBody
    public List<ServiceDto> getServices() {
        return serviceRepositoryHelper.getAllServices();
    }
}
