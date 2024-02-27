package no.nav.statusplattform.api.v3.controllers;

import no.nav.statusplattform.api.wcag.WcagJsonParser;
import no.nav.statusplattform.generated.api.KravMapEntryDto;
import no.nav.statusplattform.generated.api.WcagResultDto;
import org.actioncontroller.GET;
import org.actioncontroller.json.JsonBody;

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
