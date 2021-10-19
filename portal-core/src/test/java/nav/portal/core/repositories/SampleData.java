package nav.portal.core.repositories;

import nav.portal.core.entities.*;
import nav.portal.core.enums.ServiceType;

import java.util.*;


public class SampleData {

    String[] areaNames = {"Arbeidsøker eller permitert","Pensjon", "Venter barn", "Alene med barn", "Sykmeldt",
            "Skal søke AAP","Har mistet noen i nær famile","Sykdom i familien", "Trenger tilrettelegging",
            "Trenger økonomisk sosialhjelp", "Trenger økonomisk rådgivning", "Berørt av EØS-saken"};

    String[] dashboardNames = {"Privatperson", "Arbeidsgiver", "Sammarbeidspartner", "Et ganske så langt navn kommer her, går dette an da?", "ÆØÅ"};

    String[] serviceNames = {"Service1", "Testesrvice 2", "Æ er en tjeneste", "Øgletjeneste"};

    String[] urlStrings = {"www.random.com", "www.æøå.com", "wwww.123abc.com"};

    String[] teamNames = {"team1", "teamOrlene", "teamÆØÅ"};

    String[] descriptions = {"kort beskrivelse", "laaang beskrivelse ------- laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------", "beskrivelseÆØÅ"};

    String[] icons = {"0001","0002","0003","0004","0005","0006","0007","0008","0009","00010","0011","0012"};


public String getRandomizedDashboardName(){
    return getRandomFromArray(dashboardNames);
}

public AreaEntity getRandomizedAreaEntity(){
    return new AreaEntity()
            .setName(getRandomFromArray(areaNames))
            .setDescription(getRandomFromArray(descriptions))
            .setIcon(getRandomFromArray(icons));
}

public List<AreaEntity> getRandomLengthListOfAreaEntity(){
    Random random = new Random();
    int numberOfAreas = random.nextInt(12);
    List<AreaEntity> areas = new ArrayList<>();
    for(int i = 0; i < numberOfAreas; i++ ){
        areas.add(getRandomizedAreaEntity());
    }
    return areas;
}

public ServiceEntity getRandomizedServiceEntity() {
    return new ServiceEntity()
            .setName(getRandomFromArray(serviceNames))
            .setType(getRandomServiceType())
            .setTeam(getRandomFromArray(teamNames))
            .setMonitorlink(getRandomFromArray(urlStrings))
            .setDescription(getRandomFromArray(descriptions))
            .setLogglink(getRandomFromArray(urlStrings));
    }

private String getRandomFromArray(String[] array){
    Random random = new Random();
    return array[random.nextInt(array.length)];
}

private ServiceType getRandomServiceType(){
    Random random = new Random();
    return ServiceType.values()[random.nextInt(ServiceType.values().length)];
}


}
