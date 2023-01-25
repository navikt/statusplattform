
package no.nav.portal.rest.api.wcag;

import no.portal.web.generated.api.KravMapEntryDto;
import no.portal.web.generated.api.WcagKravDto;
import no.portal.web.generated.api.WcagResultDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class WcagJsonParserTest {



    void readAllFiles(){
        List<WcagResultDto> files = WcagJsonParser.readAllReports();
        //files.stream().filter(result -> result.getName().equals("FyllUt"));
       // files.get(0);

    }


    void getAlleKrav(){
        List<String> alleKrav = WcagJsonParser.getAllKravs();
      //  alleKrav.get(0);

    }

    void getAlleKravMap(){
        HashMap<String, ArrayList<String>> alleKrav = WcagJsonParser.getAllKravsMap();
        //alleKrav.get(0);
        List<KravMapEntryDto> alleKrav2 = WcagJsonParser.getAllKravsMapDto();
      //  alleKrav2.get(0);

    }


    void getAlleKravMapDto(){
        List<KravMapEntryDto> alleKrav = WcagJsonParser.getAllKravsMapDto();
       // alleKrav.get(0);

    }
}