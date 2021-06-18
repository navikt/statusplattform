package no.nav.portal.rest.api;

import nav.portal.core.util.Status;
import no.portal.web.generated.api.AdminAreaDto;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.StatusDto;

import java.util.*;
import java.util.stream.Collectors;

public class TestUtil {
    private static HashMap<String,List<String>> AREAS = new HashMap<>();
    private static List<AreaDto> currentStatus = Collections.emptyList();
    private static List<AdminAreaDto> DEFAULT_ADMIN_AREAS = new ArrayList<>();
    private List<AdminAreaDto> adminAreaDtos = Collections.emptyList();

    static{
        //Legg til område/area her
        AREAS.put("Arbeidssøker eller permitert", Arrays.asList("Arena","Meldekort", "Oppf. arbeid", "Søk Dagpenger"));
        AREAS.put("Pensjon", Arrays.asList("Sykmelding","Frikort", "eResept. arbeid", "Søk Dagpenger"));
        AREAS.put("Venter barn", Arrays.asList("Bidrag","Engangsstønad", "Foreldrepenger"));
        AREAS.put("Alene med barn", Arrays.asList("Din Pensjon","Pesys"));
        AREAS.put("Sykmeldt", Arrays.asList("Dine Utbetalinger","DittNAV", "nav.no", "Søknadsdialoger"));
        AREAS.put("Skal søke AAP", Arrays.asList("Dokumentløsning","Gosys", "Modiabrukerdialog", "Navet","Personkortet","WinTid"));
        AREAS.put("Har sykdom i familien", Arrays.asList("Ok"));
        AREAS.put("Trenger tilrettelegging", Arrays.asList("AAregister","AktørRegister", "Datavarehus","DKIF", "Enhetsregister","Henvendelse", "Inntektskomponenten","Joark","NORG","TPS","TSS"));
        AREAS.put("Trenger økonomisk sosialhjelp", Arrays.asList("BankID-status","Google Cloud-status", "IDporten", "MS Azure-status"));

        DEFAULT_ADMIN_AREAS.add(new AdminAreaDto().name("Arbeidssøker eller permitert")
                .beskrivelse("Arbeidssøker ...").id("ARBD").rangering(0));
        DEFAULT_ADMIN_AREAS.add(new AdminAreaDto().name("Pensjon")
                .beskrivelse("Pensjon .....").id("PENS").rangering(1));
        DEFAULT_ADMIN_AREAS.add(new AdminAreaDto().name("Venter barn")
                .beskrivelse("Venter barn  .....").id("BARN").rangering(2));

    }

/*
    public static List<AreaDto> getCurrentTestAreas(){
        if
    }
*/

    public List<AdminAreaDto> getAdminAreaDtos(){
        if(adminAreaDtos.isEmpty()){
            this.adminAreaDtos = DEFAULT_ADMIN_AREAS;
        }
        return this.adminAreaDtos;
    }

    public boolean addAdminArea(AdminAreaDto adminAreaDto){
        if(adminAreaDtos.stream().filter(dto -> dto.getId().equals(adminAreaDto.getId())).findFirst().isEmpty()){
            adminAreaDtos.add(adminAreaDto);
            return true;
        }
        return false;

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
