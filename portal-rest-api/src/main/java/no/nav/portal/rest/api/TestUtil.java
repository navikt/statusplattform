package no.nav.portal.rest.api;

import nav.portal.core.util.Status;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.ServiceDto.StatusEnum;

import java.util.*;

public class TestUtil {
    private static HashMap<String,List<String>> AREAS = new HashMap<>();
    static{
        //Legg til område/area her
        AREAS.put("Arbeid", Arrays.asList("Arena","Meldekort", "Oppf. arbeid", "Søk Dagpenger"));
        AREAS.put("Arbeid", Arrays.asList("Arena","Meldekort", "Oppf. arbeid", "Søk Dagpenger"));
    }


    public static List<AreaDto> getAllAreasWithRandomStatuses(){
        ArrayList<AreaDto> areaDtos = new ArrayList<>();
        for (String key: AREAS.keySet()){
            areaDtos.add(getOne(key));
        }
        return areaDtos;
    }
    public static AreaDto getOne(String areaName){
        AreaDto areaDto = new AreaDto();
        areaDto.setName(areaName);
        areaDto.setServices(getServicesRandomStatus(areaName));
        return areaDto;
    }


    public static List<ServiceDto> getServicesRandomStatus(String area){
        ArrayList<ServiceDto> services = new ArrayList<>();
        for (String service:AREAS.get(area)) {
            ServiceDto serviceDto = new ServiceDto();
            serviceDto.setName(service);
            serviceDto.setStatus(getRandomStatus());
            services.add(serviceDto);
        }
        return services;
    }

    public static StatusEnum getRandomStatus(){
        Random rand = new Random();
        int randInt = rand.nextInt(100);
        if(randInt < 10){
            return StatusEnum.DOWN;
        }
        if (randInt <  30 ){
            return StatusEnum.ISSUE;
        }
        return  StatusEnum.OK;
    }


}
