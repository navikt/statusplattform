
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

        DEFAULT_AREAS.put("ALBR",new AreaDto().name("Alene med barn")
                .beskrivelse("Venter barn  ...").id("ALBR").rangering(4));
        DEFAULT_AREAS.put("SYKM",new AreaDto().name("Sykmeldt")
                .beskrivelse("Sykmeldt ...").id("SYKM").rangering(5));
        DEFAULT_AREAS.put("SAAP",new AreaDto().name("Skal søke AAP")
                .beskrivelse("Skal søke AAP ...").id("SAAP").rangering(6));

        DEFAULT_AREAS.put("SYKF",new AreaDto().name("Sykdom i familien")
                .beskrivelse("Sykdom i familien  ...").id("SYKF").rangering(7));
        DEFAULT_AREAS.put("TILR",new AreaDto().name("Trenger tilrettelegging")
                .beskrivelse("Trenger tilrettelegging ...").id("TILR").rangering(8));
        DEFAULT_AREAS.put("OKOH",new AreaDto().name("Trenger økonomisk sosialhjelp")
                .beskrivelse("Trenger økonomisk sosialhjelp...").id("OKOH").rangering(9));

        DEFAULT_AREAS.put("OKHJ",new AreaDto().name("Trenger økonomisk rådgiving")
                .beskrivelse("Trenger økonomisk rådgiving ...").id("OKHJ").rangering(10));
        DEFAULT_AREAS.put("BEOS",new AreaDto().name("Berørt av EØS-saken")
                .beskrivelse("Berørt av EØS-saken ...").id("BEOS").rangering(11));
        DEFAULT_AREAS.put("MNIF",new AreaDto().name("Har mistet noen i nær familie")
                .beskrivelse("Har mistet noen i nær familie  ...").id("MNIF").rangering(12));

        //Legg til område/area her
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("ARBD"), Arrays.asList("Arena","Meldekort", "Oppf. arbeid", "Søk Dagpenger"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("PENS"), Arrays.asList("Sykmelding","Frikort", "eResept. arbeid", "Søk Dagpenger"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("BARN"), Arrays.asList("Bidrag","Engangsstønad", "Foreldrepenger"));

        AREAS_INSTANCES.put(DEFAULT_AREAS.get("ALBR"), Arrays.asList("Din Pensjon","Pesys"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("SYKM"), Arrays.asList("BankID-status","Google Cloud-status", "IDporten", "MS Azure-status"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("SAAP"), Arrays.asList("Dine Utbetalinger","DittNAV", "nav.no", "Søknadsdialoger"));

        AREAS_INSTANCES.put(DEFAULT_AREAS.get("SYKF"), Arrays.asList("Dokumentløsning","Gosys", "Modiabrukerdialog", "Navet","Personkortet","WinTid"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("TILR"), Arrays.asList("Ok"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("OKOH"), Arrays.asList("AAregister","AktørRegister", "Datavarehus","DKIF", "Enhetsregister","Henvendelse", "Inntektskomponenten","Joark","NORG","TPS","TSS"));

        AREAS_INSTANCES.put(DEFAULT_AREAS.get("OKHJ"), Arrays.asList("Dokumentløsning","Gosys", "Modiabrukerdialog", "Navet","Personkortet","WinTid"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("BEOS"), Arrays.asList("Ok"));
        AREAS_INSTANCES.put(DEFAULT_AREAS.get("MNIF"), Arrays.asList("AAregister","AktørRegister", "Datavarehus","DKIF", "Enhetsregister","Henvendelse", "Inntektskomponenten","Joark","NORG","TPS","TSS"));

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

    public boolean deleteAdminArea(AreaDto areaDto) {
        if(areaDtos.stream().filter(dto -> dto.getId().equals(areaDto.getId())).findFirst().isPresent()){
            areaDtos.remove(areaDto);
            return true;
        }
        return false;
    }


    public static List<TileDto> getAllTilesWithRandomStatuses(){
        ArrayList<TileDto> tileDtos = new ArrayList<>();
        for (AreaDto areaDto: AREAS_INSTANCES.keySet()){
            tileDtos.add(getOne(areaDto));

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
