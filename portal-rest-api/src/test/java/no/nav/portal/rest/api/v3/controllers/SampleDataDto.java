package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import no.portal.web.generated.api.DashboardDto;
import no.portal.web.generated.api.MaintenanceDto;
import no.portal.web.generated.api.ServiceDto;
import no.portal.web.generated.api.ServiceTypeDto;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SampleDataDto {
    static final ArrayList<String> dashboardNames = new ArrayList<>(Arrays.asList("Privatperson", "Arbeidsgiver", "Sammarbeidspartner", "Et ganske så langt navn kommer her, går dette an da?", "ÆØÅ", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> areaNames = new ArrayList<>(Arrays.asList("Arbeidsøker eller permitert", "Pensjon", "Venter barn", "Alene med barn", "Sykmeldt",
            "Skal søke AAP", "Har mistet noen i nær famile", "Sykdom i familien", "Trenger tilrettelegging",
            "Trenger økonomisk sosialhjelp", "Trenger økonomisk rådgivning", "Berørt av EØS-saken", "Ett navn til", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> descriptions = new ArrayList<>(Arrays.asList("kort beskrivelse", "laaang beskrivelse ------- laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------", "beskrivelseÆØÅ"));

    static final ArrayList<String> icons = new ArrayList<>(Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010", "0011", "0012"));

    static final ArrayList<String> urlStrings = new ArrayList<>(Arrays.asList("https://www.nrk.no", "https://www.nrk.no/nyheter/", "https://wwww.123abc.com", "https://wwww.ab.no", "https://wwww.ac.no", "https://wwww.ad.no", "https://wwww.ae.no", "https://wwww.af", "https://wwww.ag", "https://wwww.ah.no", "https://wwww.ai.no", "https://wwww.aj.no", "https://wwww.ak.no", "https://wwww.al.no", "https://wwww.am.no", "https://wwww.an.no", "https://wwww.ao.no"));

    static final ArrayList<String> teamNames = new ArrayList<>(Arrays.asList("team1", "teamOrlene", "teamÆØÅ", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));
    static final ArrayList<String> serviceNames = new ArrayList<>(Arrays.asList("Service1", "Testesrvice 2", "Æ er en tjeneste", "Øgletjeneste", "tjeneste123", "tjeneste213", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> maintenanceDescription = new ArrayList<>(Arrays.asList("kort maintenance", "laaang maintenance beskrivelse", "maintenaceÆØÅ"));


    public static DashboardDto getRandomizedDashboardDto() {
        return new DashboardDto()
                .name(getRandomFromArray(dashboardNames));
    }

    public static AreaEntity getRandomizedAreaEntity() {
        return new AreaEntity()
                .setName(getRandomFromArray(areaNames))
                .setDescription(getRandomFromArray(descriptions))
                .setIcon(getRandomFromArray(icons));
    }

    public static ServiceDto getRandomizedServiceDto() {
        return new ServiceDto()
                .name(getRandomFromArray(serviceNames))
                .type(getRandomServiceTypeDto())
                .team(getRandomFromArray(teamNames))
                .statusNotFromTeam(Boolean.FALSE)
                .monitorlink(getRandomFromArray(urlStrings));
    }

    public static ServiceDto getRandomizedServiceDtoWithNameNotInList(List<ServiceDto> serviceDtos) {
        List<String> usedNames = serviceDtos.stream().map(ServiceDto::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(serviceNames);
        possibleNames.removeAll(usedNames);
        return new ServiceDto()
                .name(getRandomFromArray(possibleNames))
                .type(ServiceTypeDto.TJENESTE)
                .team(getRandomFromArray(teamNames))
                .pollingUrl(getRandomFromArray(urlStrings))
                .statusNotFromTeam(Boolean.FALSE)
                .monitorlink(getRandomFromArray(urlStrings));
    }

    public static List<ServiceDto> getRandomLengthListOfServiceDto() {
        Random random = new Random();
        int numberOfAreas = random.nextInt(12);
        List<ServiceDto> serviceDtos = new ArrayList<>();
        for (int i = 0; i < numberOfAreas; i++) {
            serviceDtos.add(getRandomizedServiceDtoWithNameNotInList(serviceDtos));
        }
        return serviceDtos;
    }

    public static List<ServiceDto> getRandomLengthNonEmptyListOfServiceDto() {
        Random random = new Random();
        int numberOfServices = 1 + random.nextInt(12);
        List<ServiceDto> serviceDto = new ArrayList<>();
        for (int i = 0; i < numberOfServices; i++) {
            serviceDto.add(getRandomizedServiceDtoWithNameNotInList(serviceDto));
        }
        return serviceDto;
    }

    public static List<ServiceDto> getNonEmptyListOfServiceDto(int numberOfServiceDto) {
        List<ServiceDto> serviceDto = new ArrayList<>();
        for (int i = 0; i < numberOfServiceDto; i++) {
            serviceDto.add(getRandomizedServiceDtoWithNameNotInList(serviceDto));
        }
        return serviceDto;
    }

    public static MaintenanceDto getRandomizedMaintenanceDto(){
        return new MaintenanceDto()
                .description(getRandomFromArray(maintenanceDescription))
                .startTime(OffsetDateTime.now())
                .endTime(OffsetDateTime.now().plusDays(1));
    }

    private static ServiceTypeDto getRandomServiceTypeDto() {
        Random random = new Random();
        return ServiceTypeDto.values()[random.nextInt(ServiceType.values().length)];
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
