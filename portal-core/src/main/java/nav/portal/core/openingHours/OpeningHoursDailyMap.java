package nav.portal.core.openingHours;

import nav.portal.core.entities.OpeningHoursGroup;
import nav.portal.core.repositories.OpeningHoursRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpeningHoursDailyMap {




    //Service id - openingHoursDisplayData
    private static Map<UUID,OpeningHoursDisplayData> map = new HashMap<>();

    public static void populateMap(OpeningHoursRepository openingHoursRepository){
        Map<UUID, OpeningHoursGroup> idGroupMap = openingHoursRepository.getAllOpeningtimeForAllServicesWithOpeningTime();
        //Skriv kode her:
        // Se på OpeningHoursParser logikk getDisplayData

        //k = id, v = valid rule for date
        idGroupMap.forEach((k, v) -> map.put(k, OpeningHoursParser.getDisplayData(LocalDate.now(),v)));
    }

    public static Map<UUID,OpeningHoursDisplayData> getMap(){
        return map;
    }

}
