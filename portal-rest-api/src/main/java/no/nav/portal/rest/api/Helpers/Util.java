package no.nav.portal.rest.api.Helpers;

import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class Util {
    static  Map<Month,String> mapOfMonthsToNorwegian = new HashMap();
    static {
        Map<Month,String> map = new HashMap<>();
        map.put(Month.JANUARY, "Januar");
        map.put(Month.FEBRUARY, "Februar");
        map.put(Month.MARCH, "Mars");
        map.put(Month.APRIL, "April");
        map.put(Month.MAY, "Mai");
        map.put(Month.JUNE, "Juni");
        map.put(Month.JULY, "Juli");
        map.put(Month.AUGUST, "August");
        map.put(Month.SEPTEMBER, "September");
        map.put(Month.OCTOBER, "Oktober");
        map.put(Month.NOVEMBER, "November");
        map.put(Month.DECEMBER, "Desember");
    }




}
