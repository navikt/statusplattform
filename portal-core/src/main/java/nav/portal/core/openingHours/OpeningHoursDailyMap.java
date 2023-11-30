package nav.portal.core.openingHours;

import nav.portal.core.entities.OpeningHoursGroup;
import nav.portal.core.repositories.OpeningHoursRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpeningHoursDailyMap {

    OpeningHoursRepository openingHoursRepository;
    OpeningHoursParser openingHoursParser;

    public OpeningHoursDailyMap(OpeningHoursRepository openingHoursRepository){
        this.openingHoursRepository = openingHoursRepository;
    }

    //Service id - openingHoursDisplayData
    Map<UUID,OpeningHoursDisplayData> map = new HashMap<>();

    public void populateMap(){
        Map<UUID, OpeningHoursGroup> idGroupMap = openingHoursRepository.getAllOpeningtimeForAllServicesWithOpeningTime();
        //Skriv kode her:
        // Se på OpeningHoursParser logikk getDisplayData

        //k = id, v = valid rule for date
        idGroupMap.forEach((k, v) -> map.put(k, OpeningHoursParser.getDisplayData(LocalDate.now(),v)));
    }

    public  Map<UUID,OpeningHoursDisplayData> getMap(){
        return map;
    }

}
