package no.nav.statusplattform.api.v3.controllers;

import no.nav.statusplattform.api.Helpers.HelpTextControllerHelper;
import no.nav.statusplattform.generated.api.HelpTextDto;
import no.nav.statusplattform.generated.api.ServiceTypeDto;
import org.actioncontroller.DELETE;
import org.actioncontroller.GET;
import org.actioncontroller.POST;
import org.actioncontroller.PUT;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;

import java.util.List;

public class HelpTextController {
    private final HelpTextControllerHelper helpTextControllerHelper;


    public HelpTextController(DbContext dbContext){
        this.helpTextControllerHelper = new HelpTextControllerHelper(dbContext);

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
    public  List<HelpTextDto> getHelpTextServices() {
        return helpTextControllerHelper.getHelpTextServices();
    }

    @GET("/HelpText/Components")
    @JsonBody
    public  List<HelpTextDto> getHelpTextComponents() {
        return helpTextControllerHelper.getHelpTextComponents();
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
