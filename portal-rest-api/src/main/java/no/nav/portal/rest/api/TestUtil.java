
package no.nav.portal.rest.api;

import no.portal.web.generated.api.*;

import java.util.*;
import java.util.stream.Collectors;

public class TestUtil {
    private static HashMap<AreaDto,List<String>> AREAS_INSTANCES = new HashMap<>();
    private static List<AreaDto> currentStatus = Collections.emptyList();

    private static HashMap<String, AreaDto> DEFAULT_AREAS = new HashMap<>();
    private List<AreaDto> areaDtos = Collections.emptyList();

    static{
        DEFAULT_AREAS.put("ARBD",new AreaDto().name("Arbeidssøker eller permitert")
                .beskrivelse("Arbeidssøker ...").id("ARBD").rangering(1));
        DEFAULT_AREAS.put("PENS",new AreaDto().name("Pensjon")
                .beskrivelse("Pensjon ...").id("PENS").rangering(2));
        DEFAULT_AREAS.put("BARN",new AreaDto().name("Venter barn")
                .beskrivelse("Venter barn  ...").id("BARN").rangering(3));
        //Legg til område/area her
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("ARBD"), Arrays.asList("Arena","Meldekort", "Oppf. arbeid", "Søk Dagpenger"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("PENS"), Arrays.asList("Sykmelding","Frikort", "eResept. arbeid", "Søk Dagpenger"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("BARN"), Arrays.asList("Bidrag","Engangsstønad", "Foreldrepenger"));
        /*AREAS_INSTANCES.put("Alene med barn", Arrays.asList("Din Pensjon","Pesys"));
        AREAS_INSTANCES.put("Sykmeldt", Arrays.asList("Dine Utbetalinger","DittNAV", "nav.no", "Søknadsdialoger"));
        AREAS_INSTANCES.put("Skal søke AAP", Arrays.asList("Dokumentløsning","Gosys", "Modiabrukerdialog", "Navet","Personkortet","WinTid"));
        AREAS_INSTANCES.put("Har sykdom i familien", Arrays.asList("Ok"));
        AREAS_INSTANCES.put("Trenger tilrettelegging", Arrays.asList("AAregister","AktørRegister", "Datavarehus","DKIF", "Enhetsregister","Henvendelse", "Inntektskomponenten","Joark","NORG","TPS","TSS"));
        AREAS_INSTANCES.put("Trenger økonomisk sosialhjelp", Arrays.asList("BankID-status","Google Cloud-status", "IDporten", "MS Azure-status"));
    */


    }

/*
    public static List<AreaDto> getCurrentTestAreas(){
        if
    }
*/

    public List<AreaDto> getAreaDtos(){
        if(areaDtos.isEmpty()){
            this.areaDtos = DEFAULT_AREAS.values().stream().collect(Collectors.toList());
        }
        return this.areaDtos;
    }

    public boolean addAdminArea(AreaDto areaDto){
        if(areaDtos.stream().filter(dto -> dto.getId().equals(areaDto.getId())).findFirst().isEmpty()){
            areaDtos.add(areaDto);
            return true;
        }
        return false;

    }


    public static List<TileDto> getAllTilesWithRandomStatuses(){
        ArrayList<TileDto> tileDtos = new ArrayList<>();
        for (AreaDto key: AREAS_INSTANCES.keySet()){
            tileDtos.add(getOne(key));
        }
        return tileDtos;
    }
    public static TileDto getOne(AreaDto areaDto){
        TileDto tileDto = new TileDto();
        tileDto.setArea(areaDto);
        tileDto.setServices(getServicesRandomStatus(areaDto));
        return tileDto;
    }


    public static List<ServiceDto> getServicesRandomStatus(AreaDto areaDto){
        ArrayList<ServiceDto> services = new ArrayList<>();
        for (String service: AREAS_INSTANCES.get(areaDto)) {
            ServiceDto serviceDto = new ServiceDto();
            serviceDto.setName(service);
            serviceDto.setStatus(getRandomStatus());
            services.add(serviceDto);
        }
        return services;
    }

    public static StatusDto getRandomStatus(){
        Random rand = new Random();
        int randInt = rand.nextInt(100);
        if(randInt < 5){
            return StatusDto.DOWN;
        }
        if (randInt <  15 ){
            return StatusDto.ISSUE;
        }
        return  StatusDto.OK;
    }


}
