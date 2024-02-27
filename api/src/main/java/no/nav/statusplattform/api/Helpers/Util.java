package no.nav.statusplattform.api.Helpers;

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
