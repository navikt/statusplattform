package no.nav.portal.rest.api.v3.controllers;

import no.portal.web.generated.api.MaintenanceDto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class SampleDataDto {
    static final ArrayList<String> maintenanceDescription = new ArrayList<>(Arrays.asList("kort maintenance", "laaang maintenance beskrivelse", "maintenaceÆØÅ"));
    public static MaintenanceDto getRandomizedMaintenanceDto(UUID serviceUuid){
        return new MaintenanceDto()
                .serviceId(serviceUuid)
                .description(getRandomFromArray(maintenanceDescription))
                .startTime(OffsetDateTime.now())
                .endTime(OffsetDateTime.now().plusDays(1));

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
