package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.*;
import nav.portal.core.enums.OpsMessageSeverity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.enums.ServiceType;
import no.portal.web.generated.api.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class SampleDataDto {
    static final ArrayList<String> dashboardNames = new ArrayList<>(Arrays.asList("Privatperson", "Arbeidsgiver", "Samarbeidspartner", "Et ganske så langt navn kommer her, går dette an da?", "ÆØÅ", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> areaNames = new ArrayList<>(Arrays.asList("Arbeidsøker eller permitert", "Pensjon", "Venter barn", "Alene med barn", "Sykmeldt",
            "Skal søke AAP", "Har mistet noen i nær famile", "Sykdom i familien", "Trenger tilrettelegging",
            "Trenger økonomisk sosialhjelp", "Trenger økonomisk rådgivning", "Berørt av EØS-saken", "Ett navn til", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> descriptions = new ArrayList<>(Arrays.asList("kort beskrivelse", "laaang beskrivelse ------- laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------", "beskrivelseÆØÅ"));

    static final ArrayList<String> icons = new ArrayList<>(Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010", "0011", "0012"));

    static final ArrayList<String> urlStrings = new ArrayList<>(Arrays.asList("https://www.nrk.no", "https://www.nrk.no/nyheter/", "https://wwww.123abc.com", "https://wwww.ab.no", "https://wwww.ac.no", "https://wwww.ad.no", "https://wwww.ae.no", "https://wwww.af", "https://wwww.ag", "https://wwww.ah.no", "https://wwww.ai.no", "https://wwww.aj.no", "https://wwww.ak.no", "https://wwww.al.no", "https://wwww.am.no", "https://wwww.an.no", "https://wwww.ao.no"));

    static final ArrayList<String> teamNames = new ArrayList<>(Arrays.asList("team1", "teamOrlene", "teamÆØÅ", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> serviceNames = new ArrayList<>(Arrays.asList("Service1", "Testesrvice 2", "Æ er en tjeneste", "Øgletjeneste", "tjeneste123", "tjeneste213", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> headersForOpsMessages = new ArrayList<>(Arrays.asList("Trøbbel i tårnet", "Nå går det gæli", "Spark meg baklengs oppi fuglekassa", "For the memes", "Det blåser nordavind fra alle kanter"));

    static final ArrayList<String> infoTextForOpsMessages = new ArrayList<>(Arrays.asList("Noen har gjort noe alvorlig galt", "En ape har trengt seg inn på systemet. Det ligger bananer overalt", "WW3, oh no", "Facebook har sendt jorda inn i sola", "Elon Musk har kjøpt opp Nav"));

    static final ArrayList<OpsMessageSeverity> opsMessageSeverity = new ArrayList<>(Arrays.asList(OpsMessageSeverity.DOWN, OpsMessageSeverity.OK,OpsMessageSeverity.ISSUE,OpsMessageSeverity.NEUTRAL));


    static final ArrayList<String> maintenanceDescription = new ArrayList<>(Arrays.asList("kort maintenance", "laaang maintenance beskrivelse", "maintenaceÆØÅ"));


    public static DashboardDto getRandomizedDashboardDto() {
        return new DashboardDto()
                .name(getRandomFromArray(dashboardNames));
    }

    public static AreaDto getRandomizedAreaDto() {
        return new AreaDto()
                .name(getRandomFromArray(areaNames))
                .description(getRandomFromArray(descriptions))
                .icon(getRandomFromArray(icons));
    }

    public static AreaDto getRandomizedAreaDtoWithNameNotInList(List<AreaDto> areasDto) {
        List<String> usedNames = areasDto.stream().map(AreaDto::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(areaNames);
        possibleNames.removeAll(usedNames);
        return new AreaDto()
                .name(getRandomFromArray(possibleNames))
                .icon(getRandomFromArray(icons))
                .description(getRandomFromArray(descriptions));
    }

    public static List<AreaDto> getRandomLengthListOfAreaDto() {
        Random random = new Random();
        int numberOfAreasDto = 1 + random.nextInt(12);
        List<AreaDto> areasDto = new ArrayList<>();
        for (int i = 0; i <= numberOfAreasDto; i++) {
            areasDto.add(getRandomizedAreaDtoWithNameNotInList(areasDto));
        }
        return areasDto;
    }

    public static List<AreaDto> getNonEmptyListOfAreaDto(int length) {
        List<AreaDto> areasDto = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            areasDto.add(getRandomizedAreaDtoWithNameNotInList(areasDto));
        }
        return areasDto;
    }

    public static SubAreaDto getRandomizedSubAreaDto() {
        return new SubAreaDto()
                .name(getRandomFromArray(areaNames));
    }

    public static SubAreaDto getRandomizedSubAreaDtoWithNameNotInList(List<SubAreaDto> subAreasDto) {
        List<String> usedNames = subAreasDto.stream().map(SubAreaDto::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(areaNames);
        possibleNames.removeAll(usedNames);
        return new SubAreaDto()
                .name(getRandomFromArray(possibleNames));
    }

    public static List<SubAreaDto> getRandomLengthListOfSubAreaDto() {
        Random random = new Random();
        int numberOfSubAreas = random.nextInt(12);
        List<SubAreaDto> subAreasDto = new ArrayList<>();
        for (int i = 0; i <= numberOfSubAreas; i++) {
            subAreasDto.add(getRandomizedSubAreaDtoWithNameNotInList(subAreasDto));
        }
        return subAreasDto;
    }

    public static List<SubAreaDto> getRandomLengthNonEmptyListOfSubAreaDto() {
        Random random = new Random();
        int numberOfSubAreasDto = 1 + random.nextInt(12);
        List<SubAreaDto> subAreasDto = new ArrayList<>();
        for (int i = 0; i <= numberOfSubAreasDto; i++) {
            subAreasDto.add(getRandomizedSubAreaDtoWithNameNotInList(subAreasDto));
        }
        return subAreasDto;
    }

    public static List<SubAreaDto> getNonEmptyListOfSubAreaDto(int length) {
        Random random = new Random();
        List<SubAreaDto> subAreasDto = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            subAreasDto.add(getRandomizedSubAreaDtoWithNameNotInList(subAreasDto));
        }
        return subAreasDto;
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

    public static RecordDto getRandomizedRecordDto() {
        return new RecordDto()
                .timestamp(OffsetDateTime.from(ZonedDateTime.now()))
                .status(getRandomStatusDto())
                .responseTime(getRandomResponseTime());
    }

    public static RecordDto getRandomizedRecordDtoForService(ServiceDto serviceDto) {
        return new RecordDto()
                .serviceId(serviceDto.getId())
                .timestamp(OffsetDateTime.from(ZonedDateTime.now()))
                .status(getRandomStatusDto())
                .responseTime(getRandomResponseTime());
    }

    public static List<RecordDto> getRandomizedRecordEntitiesForService(ServiceDto serviceDto) {
        List<RecordDto> recordsDto = new ArrayList<>();
        Random random = new Random();
        int numberOfRecords = 1 + random.nextInt(12);
        for (int i = 0; i < numberOfRecords; i++) {
            recordsDto.add(getRandomizedRecordDtoForService(serviceDto));
        }
        return recordsDto;
    }

    public static MaintenanceDto getRandomizedMaintenanceDto(){
        return new MaintenanceDto()
                .description(getRandomFromArray(maintenanceDescription))
                .startTime(OffsetDateTime.now())
                .endTime(OffsetDateTime.now().plusDays(1));
    }

    public static OPSmessageDto getRandomOPSMessageDto() {
        Random random = new Random();
        return new OPSmessageDto()
                .internalHeader(getRandomFromArray(headersForOpsMessages))
                .internalMessage(getRandomFromArray(infoTextForOpsMessages))
                .startTime(OffsetDateTime.from(getZonedDateTimeNowWithOutDecimals()))
                .endTime(OffsetDateTime.from(getZonedDateTimeNowWithOutDecimals()).plusDays(2))
                .severity(getRandomOpsMessageDtoSeverity())
                .onlyShowForNavEmployees(random.nextBoolean())
                .isActive(true);
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

    private static ServiceStatus getRandomServiceStatus() {
        Random random = new Random();
        return ServiceStatus.values()[random.nextInt(ServiceStatus.values().length)];
    }

    private static StatusDto getRandomStatusDto() {
        Random random = new Random();
        return StatusDto.values()[random.nextInt(StatusDto.values().length)];
    }

    private static Integer getRandomResponseTime() {
        List<Integer> responseTime = Arrays.asList(10, 20, 30, 100, 1000);
        Random random = new Random();
        return responseTime.get(random.nextInt(responseTime.size()));
    }

    private static ZonedDateTime getZonedDateTimeNowWithOutDecimals(){
        return ZonedDateTime.of(LocalDate.now(), LocalTime.of(0,0), ZoneId.of("Europe/Paris"));
    }

    private static OPSmessageDto.SeverityEnum getRandomOpsMessageDtoSeverity() {
        Random random = new Random();
        return OPSmessageDto.SeverityEnum.values()[random.nextInt(OPSmessageDto.SeverityEnum.values().length)];
    }

}
