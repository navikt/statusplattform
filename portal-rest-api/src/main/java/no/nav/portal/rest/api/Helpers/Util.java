package no.nav.portal.rest.api.Helpers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.ServiceRepository;
import no.portal.web.generated.api.ServiceDto;
import org.apache.commons.validator.routines.UrlValidator;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class Util {
    static Map<Month, String> mapOfMonthsToNorwegian = new HashMap();


    static {
        mapOfMonthsToNorwegian.put(Month.JANUARY, "Januar");
        mapOfMonthsToNorwegian.put(Month.FEBRUARY, "Februar");
        mapOfMonthsToNorwegian.put(Month.MARCH, "Mars");
        mapOfMonthsToNorwegian.put(Month.APRIL, "April");
        mapOfMonthsToNorwegian.put(Month.MAY, "Mai");
        mapOfMonthsToNorwegian.put(Month.JUNE, "Juni");
        mapOfMonthsToNorwegian.put(Month.JULY, "Juli");
        mapOfMonthsToNorwegian.put(Month.AUGUST, "August");
        mapOfMonthsToNorwegian.put(Month.SEPTEMBER, "September");
        mapOfMonthsToNorwegian.put(Month.OCTOBER, "Oktober");
        mapOfMonthsToNorwegian.put(Month.NOVEMBER, "November");
        mapOfMonthsToNorwegian.put(Month.DECEMBER, "Desember");
    }




}
