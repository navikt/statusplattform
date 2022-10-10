package no.nav.portal.rest.api.v3.controllers;

import no.portal.web.generated.api.DashboardDto;
import no.portal.web.generated.api.MaintenanceDto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class SampleDataDto {
    static final ArrayList<String> dashboardNames = new ArrayList<>(Arrays.asList("Privatperson", "Arbeidsgiver", "Sammarbeidspartner", "Et ganske så langt navn kommer her, går dette an da?", "ÆØÅ", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));
    static final ArrayList<String> maintenanceDescription = new ArrayList<>(Arrays.asList("kort maintenance", "laaang maintenance beskrivelse", "maintenaceÆØÅ"));


    public static MaintenanceDto getRandomizedMaintenanceDto(UUID serviceUuid){
        return new MaintenanceDto()
                .serviceId(serviceUuid)
                .description(getRandomFromArray(maintenanceDescription))
                .startTime(OffsetDateTime.now())
                .endTime(OffsetDateTime.now().plusDays(1));

    }

    public static DashboardDto getRandomizedDashboardDto() {
        return new DashboardDto()
                .name(getRandomFromArray(dashboardNames));
    }

    private static String getRandomFromArray(ArrayList<String> array) {
        if (array.size() == 0) {
            //Hit skal man ikke komme
            return null;
        }
        Random random = new Random();
        return array.get(random.nextInt(array.size()));
    }



}
