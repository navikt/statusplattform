package no.nav.portal.rest.api.v3.controllers;

import no.nav.portal.rest.api.wcag.WcagJsonParser;
import no.portal.web.generated.api.KravMapEntryDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.WcagKravDto;
import no.portal.web.generated.api.WcagResultDto;
import org.actioncontroller.GET;
import org.actioncontroller.json.JsonBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WcagController {
    @GET("/Wcag/Tjeneste")
    @JsonBody
    public List<WcagResultDto> getWcagTjenester() {
        return WcagJsonParser.readAllReports();
    }

    @GET("/Wcag/Krav")
    @JsonBody
    public List<KravMapEntryDto> getWcagKrav() {
        return WcagJsonParser.getAllKravsMapDto();
    }
}
