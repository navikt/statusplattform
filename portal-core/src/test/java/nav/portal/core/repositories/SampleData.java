package nav.portal.core.repositories;

import nav.portal.core.entities.*;
import nav.portal.core.enums.ServiceType;

import java.util.Random;
import java.util.UUID;


public class SampleData {

    String[] areaNames = {"Arbeidsøker eller permitert","Pensjon", "Venter barn", "Alene med barn", "Sykmeldt",
            "Skal søke AAP","Har mistet noen i nær famile","Sykdom i familien", "Trenger tilrettelegging",
            "Trenger økonomisk sosialhjelp", "Trenger økonomisk rådgivning", "Berørt av EØS-saken"};

    String[] dashboardNames = {"Privatperson", "Arbeidsgiver", "Sammarbeidspartner"};

    String[] serviceNames = {"Service1", "Testservice 2", "Æ er en tjeneste", "Øgletjeneste"};

    String[] urlStrings = {"www.random.com", "www.æøå.com", "wwww.123abc.com"};

    String[] teamNames = {"team1", "teamOrlene", "teamÆØÅ"};

    String[] descriptions = {"kort beskrivelse", "laaang beskrivelse ------- laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------", "beskrivelseÆØÅ"};




public ServiceEntity getServiceEntity() {
    return new ServiceEntity()
            .setId(UUID.randomUUID())
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
