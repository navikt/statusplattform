package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.enums.ServiceType;
import nav.portal.core.repositories.HelpTextRepository;
import no.nav.portal.rest.api.Helpers.HelpTextControllerHelper;
import no.nav.portal.rest.api.Helpers.StatusUrlValidator;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;
import java.util.UUID;

public class HelpTextController {
    private final HelpTextControllerHelper helpTextControllerHelper;
    private final HelpTextRepository helpTextRepository;

    public HelpTextController(DbContext dbContext){
        this.helpTextControllerHelper = new HelpTextControllerHelper(dbContext);
        this.helpTextRepository = new HelpTextRepository(dbContext);
    }

    @POST("/HelpText")
    @JsonBody
    public HelpTextDto newHelpText(@JsonBody HelpTextDto helpTextDto) {
        return helpTextControllerHelper.save(helpTextDto);
    }

    @PUT("/HelpText")
    @JsonBody
    public void updateHelpText(@JsonBody HelpTextDto helpTextDto) {
        helpTextControllerHelper.update(helpTextDto);
    }

    @GET("/HelpText/:HelpText_number/:HelpText_type")
    @JsonBody
    public HelpTextDto getHelpText(@PathParam("HelpText_number") int helpText_number,
                                   @PathParam("HelpText_type") ServiceTypeDto helpText_type) {
        return helpTextControllerHelper.retrieveOneHelpText(helpText_number, helpText_type);
    }

    @GET("/HelpText/Services")
    @JsonBody
    public  List<HelpTextDto> getAllServices() {
        return helpTextControllerHelper.getAllServices();
    }

    @GET("/HelpText/Components")
    @JsonBody
    public  List<HelpTextDto> getAllComponents() {
        return helpTextControllerHelper.getAllComponents();
    }

    @DELETE("/HelpText")
    @JsonBody
    public void deleteHelpText(@JsonBody HelpTextDto helpTextDto) {
        helpTextControllerHelper.delete(helpTextDto);
    }

    @GET("/HelpText")
    @JsonBody
    public List<HelpTextDto> getAllHelpTexts() {
        return helpTextControllerHelper.getAllHelpTexts();
    }
}
