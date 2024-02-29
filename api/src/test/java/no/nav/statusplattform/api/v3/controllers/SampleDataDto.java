package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.enums.ServiceType;
import no.nav.statusplattform.generated.api.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SampleDataDto {
    static final ArrayList<String> dashboardNames = new ArrayList<>(Arrays.asList("Ekstern", "Intern", "Ekstenpartner", "Dette er et velidig langt navn", "blahblahaa", "blahblahaab", "blahblahaac", "blahblahaad", "blahblahaae", "blahblahaaf", "blahblahaag", "blahblahaah", "blahblahaai", "blahblahaaj", "blahblahaak", "blahblahaal", "blahblahaam", "blahblahaan", "blahblahaao", "blahblahaap", "blahblahaaq", "blahblahaar", "blahblahaas", "blahblahaat"));

    static final ArrayList<String> areaNames = new ArrayList<>(Arrays.asList("Permitert igjen", "Pensjon", "Gravid uten å vite om det", "aleneforsørger", "sykemeldt i oppsigelsestiden",
            "AAP", "Skilsmisse", "Syk familien", "Trenger hjelp hos Nav",
            "Trenger penger", "Trenger økonomisk help", "Berørt av depresjon", "Whatever", "cb", "cc", "cd", "ce", "cf", "cg", "ch", "ci", "cj", "ck", "cl", "cm", "cn", "co", "cp", "cq", "cr", "cs", "ct"));

    static final ArrayList<String> descriptions = new ArrayList<>(Arrays.asList("short description", "Too horrible consider followed may differed age. An rest if more five mr of. Age just her rank met down way. If suspicion do departure at extremely he believing. Do know said mind do rent they oh hope of.", "Rather in lasted no within no"));

    static final ArrayList<String> icons = new ArrayList<>(Arrays.asList("00010", "00020", "00030", "00040", "00050", "00060", "00070", "00080", "00090", "00100", "00110", "00120"));

    static final ArrayList<String> urlStrings = new ArrayList<>(Arrays.asList("https://www.vg.no", "https://www.aftenposten.no", "https://wwww.abc123.com", "https://wwww.ba.no", "https://wwww.bb.no", "https://wwww.bc.no", "https://wwww.bd.no", "https://wwww.be.no", "https://wwww.bf.no", "https://wwww.bg.no", "https://wwww.bh.no", "https://wwww.bi.no", "https://wwww.bj.no", "https://wwww.bk.no", "https://wwww.bl.no", "https://wwww.bm.no", "https://wwww.bn.no"));

    static final ArrayList<String> teamNames = new ArrayList<>(Arrays.asList("team 1", "team 2", "team 3", "Team 4", "team 5", "team 6", "team 7", "team 8", "team 9", "team 10", "team A", "team B", "team c", "team D", "team E", "team F", "team G", "team H", "team I", "team J", "team K", "team L"));

    static final ArrayList<String> serviceNames = new ArrayList<>(Arrays.asList("ServiceName1", "ServiceName2", "ServiceName3", "ServiceName4", "ServiceName5", "ServiceName6", "ServiceName7", "ServiceName8", "ServiceName9", "ServiceName10", "ServiceName11", "ServiceName12", "Service ah", "Service ai", "Service aj", "Service ak", "Service al", "Service am", "Service an", "Service ao", "Service ap", "Service aq", "Service ar", "Service as", "Service at"));

    static final ArrayList<String> headersForOpsMessages = new ArrayList<>(Arrays.asList("Garbage in garbage out", "Glitch", "Page fault", "Logic error", "Known error", "Abnormal end", "Exceptional exception"));

    static final ArrayList<String> infoTextForOpsMessages = new ArrayList<>(Arrays.asList("Input data produces nonsense output", "A short-lived fault in a system", "an exception that the memory management unit ", "a bug in a program that causes it to operate incorrectly", "A little disruptive impact on the end user or a known work around"));

    static final ArrayList<String> maintenanceDescription = new ArrayList<>(Arrays.asList("Fix of the glitch", "Making the exceptional exception unexceptional", "Update to version whatever"));

    static final Map<String, String> namesAndRules =  Map.ofEntries(
            Map.entry("Christmas day", "24.12.???? ? ? 00:00-00:00"),
            Map.entry("Boxing Day", "24.12.???? ? ? 00:00-00:00"),
            Map.entry("Good Friday", "07.04.2023 ? ? 00:00-00:00"),
            Map.entry("Easter Monday", "10.04.2023 ? ? 00:00-00:00"),
            Map.entry("May day","01.05.???? ? ? 00:00-00:00"),
            Map.entry("National day","17.05.???? ? ? 00:00-00:00"),
            Map.entry("Kristihimmelfartsdag","18.05.2023 ? ? 00:00-00:00"),
            Map.entry("Annen pinse dag","29.05.2023 ? ? 00:00-00:00"),
            Map.entry("Early Closing Winter","19.01.2023 ? ? 07:00-15:00"),
            Map.entry("Early Closing Spring","13.04.2023 ? ? 07:00-15:00"),
            Map.entry("Early Closing Summer","20.07.2023 ? ? 07:00-15:00"),
            Map.entry("Early Closing Autumn","19.10.2023 ? ? 07:00-15:00"),
            Map.entry("LastDayOfTheMonth","??.??.???? L ? 07:00-18:00"),
            Map.entry("Specified run days", "??.??.???? 1-5,15-20 ? 07:00-21:00"),
            Map.entry("Normal work days", "??.??.???? ? 1-5 07:30-17:00"));

    static final Map<String, String> holidayRules =  Map.ofEntries(
            Map.entry("Good Friday", "07.04.2023 ? ? 00:00-00:00"),
            Map.entry("Easter Monday", "10.04.2023 ? ? 00:00-00:00"),
            Map.entry("May day","01.05.???? ? ? 00:00-00:00"),
            Map.entry("National day","17.05.???? ? ? 00:00-00:00"),
            Map.entry("Kristihimmelfartsdag","18.05.2023 ? ? 00:00-00:00"),
            Map.entry("Annen pinse dag","29.05.2023 ? ? 00:00-00:00"),
            Map.entry("Christmas day", "24.12.???? ? ? 00:00-00:00"),
            Map.entry("Boxing Day", "25.12.???? ? ? 00:00-00:00"));

    static final Map<String, String> earlyClosingRules =  Map.ofEntries(
            Map.entry("Early Closing Winter","19.01.2023 ? ? 07:00-15:00"),
            Map.entry("Early Closing Spring","13.04.2023 ? ? 07:00-15:00"),
            Map.entry("Early Closing Summer","20.07.2023 ? ? 07:00-15:00"),
            Map.entry("Early Closing Autumn","19.10.2023 ? ? 07:00-15:00"),
            Map.entry("Open Saturday","05.08.2023 ? 6 10:00-15:00"));

    static final Map<String, String> collaborativeMaintenanceRules =  Map.ofEntries(
            Map.entry("LastDayOfTheMonth","??.??.???? L ? 07:00-18:00"));

    static final Map<String, String> localMaintenanceRules =  Map.ofEntries(
            Map.entry("local maintenance", "??.??.???? 1-5,15-20 ? 07:00-16:00"));

    static final ArrayList<String> helpTextDescriptions = new ArrayList<>(Arrays.asList(
            "Navnet på komponenten slik den omtales ut mot brukerne av komponenten",
            "Navnet på tjenesten slik den omtales ut mot brukerne av tjenesten",
            "Navnet på team slik det er skrevet i Teamkatalogen",
            "Link til et eventuelt dashboard eller monitor med mer detaljert informasjon. Eksempelvis Grafana dashboard",
            "URL til statusendepunkt som Statusplattformen skal polle for status",
            "Her kan man legge inn andre komponenter det er avhengigheter til. Informasjon om status på disse vil da vises i komponentbildet. Velg i liste og klikk Legg til for hver komponent.",
            "Her legger man inn tjenester hvor komponeten skal vises. Velg i liste og klikk Legg til for hver tjeneste."));


    static final ArrayList<String> groupDescription = new ArrayList<>(Arrays.asList("National Holidays", "Collaborative maintenance", "local maintenance", "Early closing"));
    static final Map<String, String> baseRules =  Map.ofEntries(
            Map.entry("Normal work days", "??.??.???? ? 1-5 07:30-17:00"));

    public static DashboardDto getRandomizedDashboardDto() {
        return new DashboardDto()
                .name(getRandomFromArray(dashboardNames));
    }

    public static List<DashboardDto> getDashboardDtos() {
        Random random = new Random();
        int numberOfDashboardDtos = 1+ random.nextInt( dashboardNames.size()-1);
        List<DashboardDto> dashboardDtos = new ArrayList<>();
        for (int i = 0; i <= numberOfDashboardDtos; i++) {
            dashboardDtos.add(new DashboardDto() .name(dashboardNames.get(i)));
        }
        return dashboardDtos;
    }


    public static AreaDto getRandomizedAreaDto() {
        return new AreaDto()
                .name(getRandomFromArray(areaNames))
                .description(getRandomFromArray(descriptions))
                .containsComponents(false);
    }

    public static AreaDto getRandomizedAreaDtoWithNameNotInList(List<AreaDto> areasDto) {
        List<String> usedNames = areasDto.stream().map(AreaDto::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(areaNames);
        possibleNames.removeAll(usedNames);
        return new AreaDto()
                .name(getRandomFromArray(possibleNames))
                .containsComponents(false)
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
                .type(ServiceTypeDto.TJENESTE)
                .team(getRandomFromArray(teamNames))
                .statusNotFromTeam(Boolean.FALSE)
                .pollingOnPrem(Boolean.FALSE)
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
                .pollingOnPrem(Boolean.FALSE)
                .monitorlink(getRandomFromArray(urlStrings));
    }

    public static List<ServiceDto> getRandomLengthListOfServiceDto() {
        Random random = new Random();
        int numberOfAreas = 1+ random.nextInt(12);
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
                .id(UUID.randomUUID())
                .timestamp(offsetNow())
                .status(getRandomStatusDto())
                .responseTime(getRandomResponseTime());
    }

    public static RecordDto getRandomizedRecordDtoForService(ServiceDto serviceDto) {
        return new RecordDto()
                .id(UUID.randomUUID())
                .serviceId(serviceDto.getId())
                .timestamp(offsetNow())
                .status(getRandomStatusDto())
                .responseTime(getRandomResponseTime());
    }

    public static List<RecordDto> getRandomizedRecordDtosForService(ServiceDto serviceDto) {
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
                .startTime(offsetNow())
                .endTime(offsetNow().plusDays(2))
                .severity(getRandomOpsMessageDtoSeverity())
                .onlyShowForNavEmployees(random.nextBoolean())
                .isActive(true);
    }

    public static OPSmessageDto getRandomizedOPSMessageDtoWithNameNotInList(List<OPSmessageDto> opsMessageDtos) {
        Random random = new Random();
        int startTime = random.nextInt(3) ;// max = 0 min = -3 days
        int endTime = random.nextInt(3) + 1;
        List<String> usedHeaders = opsMessageDtos.stream().map(OPSmessageDto::getInternalHeader).collect(Collectors.toList());
        ArrayList<String> possibleHeaders = new ArrayList<>(headersForOpsMessages);
        possibleHeaders.removeAll(usedHeaders);
        return new OPSmessageDto()
                .internalHeader(getRandomFromArray(possibleHeaders))
                .internalMessage(getRandomFromArray(infoTextForOpsMessages))
                .startTime(offsetNow().minusDays(startTime))
                .endTime(offsetNow().plusDays(endTime))
                .severity(getRandomOpsMessageDtoSeverity())
                .onlyShowForNavEmployees(random.nextBoolean())
                .isActive(true);
    }

    public static List<OPSmessageDto> getRandomLengthListOfOPSMessageDto() {
        Random random = new Random();
        int numberOfOpsMessageDto = 1 + random.nextInt(headersForOpsMessages.size()-1);
        List<OPSmessageDto> opsMessageDtos = new ArrayList<>();
        for (int i = 0; i <= numberOfOpsMessageDto; i++) {
            opsMessageDtos.add(getRandomizedOPSMessageDtoWithNameNotInList(opsMessageDtos));
        }
        return opsMessageDtos;
    }

    public static List<OPSmessageDto> getNonEmptyListOfOpsMessageDto(int length) {
        List<OPSmessageDto> opsMessageDtos = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            opsMessageDtos.add(getRandomizedOPSMessageDtoWithNameNotInList(opsMessageDtos));
        }
        return opsMessageDtos;
    }

    public static OHRuleDto getOHRuleDtoSimple() {
        return new OHRuleDto()
                .name("Normal work days")
                .rule(namesAndRules.get("Normal work days"));
    }

    public static OHRuleDto getRandomizedOHRuleDto() {
        String randomKey = getRandomFromKey(new ArrayList<>(namesAndRules.keySet()));
        return new OHRuleDto()
                .name(randomKey)
                .rule(namesAndRules.get(randomKey));
    }

    public static List<OHRuleDto> getRandomRulesDto() {
        Random random = new Random();
        int numberOfRulesDtos = 1+ random.nextInt( namesAndRules.size()-1);
        List<String>names = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> oHRuleDtos = new ArrayList<>();
        for (int i = 0; i <= numberOfRulesDtos; i++) {
            oHRuleDtos.add(new OHRuleDto()
                    .name(names.get(i))
                    .rule(namesAndRules.get(names.get(i))));
        }
        return oHRuleDtos;
    }

    public static List<OHRuleDto> getRulesDto() {
        List<String>names = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> oHRuleDtos = new ArrayList<>();
        for (int i = 0; i < namesAndRules.size(); i++) {
            oHRuleDtos.add(new OHRuleDto()
                    .name(names.get(i))
                    .rule(namesAndRules.get(names.get(i))));
        }
        return oHRuleDtos;
    }

    public static List<OHRuleDto> getOrderedRules() {
        LinkedHashMap<String, String> orderedNamesAndRules = new LinkedHashMap<>();

        orderedNamesAndRules.put("Christmas day", "24.12.???? ? 1-5 09:00-14:00");
        orderedNamesAndRules.put("National day", "17.05.???? ? ? 00:00-00:00");
        orderedNamesAndRules.put("LastDayOfTheMonth", "??.??.???? L ? 07:00-18:00");
        orderedNamesAndRules.put("Specified run days", "??.??.???? 1-5,15-20 ? 07:00-21:00");
        orderedNamesAndRules.put("Normal work days", "??.??.???? ? 1-5 07:30-17:00");

        List<String> names = new ArrayList<>(orderedNamesAndRules.keySet());
        List<OHRuleDto> oHRuleDtos = new ArrayList<>();
        for (int i = 0; i < orderedNamesAndRules.size(); i++) {
            oHRuleDtos.add(new OHRuleDto()
                    .name(names.get(i))
                    .rule(orderedNamesAndRules.get(names.get(i))));
        }
        return oHRuleDtos;
    }

    public static List<List<OHRuleDto>> getListOfRules () {
        List<List<OHRuleDto>> listOfOrderedRules = new ArrayList<>();
        listOfOrderedRules.add(getOrderedRules(new LinkedHashMap<>(baseRules)));
        listOfOrderedRules.add(getOrderedRules(new LinkedHashMap<>(localMaintenanceRules)));
        listOfOrderedRules.add(getOrderedRules(new LinkedHashMap<>(collaborativeMaintenanceRules)));
        listOfOrderedRules.add(getOrderedRules(new LinkedHashMap<>(earlyClosingRules)));
        listOfOrderedRules.add(getOrderedRules(new LinkedHashMap<>(holidayRules)));
        return listOfOrderedRules;
    }

    private static List<OHRuleDto> getOrderedRules(LinkedHashMap<String, String> orderedNamesAndRules) {

        List<String> names = new ArrayList<>(orderedNamesAndRules.keySet());
        List<OHRuleDto> oHRuleDtos = new ArrayList<>();
        for (int i = 0; i < orderedNamesAndRules.size(); i++) {
            oHRuleDtos.add(new OHRuleDto()
                    .name(names.get(i))
                    .rule(orderedNamesAndRules.get(names.get(i))));
        }
        return oHRuleDtos;
    }

    public static List<OHRuleDto> getNonEmptyListOfOHRuleDto(int length) {
        List<OHRuleDto> oHRulesDto = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            oHRulesDto.add(getRandomizedOHRuleDtoWithNameNotInList(oHRulesDto));
        }
        return oHRulesDto;
    }

    public static OHRuleDto getRandomizedOHRuleDtoWithNameNotInList(List<OHRuleDto> oHRulesDto) {
        List<String> usedNames = oHRulesDto.stream().map(OHRuleDto::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(namesAndRules.keySet());
        possibleNames.removeAll(usedNames);
        String randomKey = getRandomFromKey(new ArrayList<>(possibleNames));
        return new OHRuleDto()
                .name(randomKey)
                .rule(namesAndRules.get(randomKey));
    }

    public static List<OHRuleDto> getRandomLengthListOfOHRuleDto() {
        Random random = new Random();
        int numberOfOHRulesDto = 1 + random.nextInt(4);
        List<OHRuleDto> oHRulesDto = new ArrayList<>();
        for (int i = 0; i <= numberOfOHRulesDto; i++) {
            oHRulesDto.add(getRandomizedOHRuleDtoWithNameNotInList(oHRulesDto));
        }
        return oHRulesDto;
    }

    public static OHGroupThinDto getBasicGroupThinDto() {
        return new OHGroupThinDto()
                .name("Basic")
                .rules(Collections.EMPTY_LIST);
    }

    public static OHGroupThinDto getRandomizedOHGroupThinDto() {
        return new OHGroupThinDto()
                .name(getRandomFromArray(groupDescription))
                .rules(Collections.EMPTY_LIST);
    }

    public static List<OHGroupThinDto> getGroupsThinDto() {
        Random random = new Random();
        int numberOfGroupsThinDtos = 1+ random.nextInt( groupDescription.size()-1);
        List<OHGroupThinDto> oHGroupThinDtos = new ArrayList<>();
        for (int i = 0; i <= numberOfGroupsThinDtos; i++) {
            oHGroupThinDtos.add(new OHGroupThinDto()
                    .name(groupDescription.get(i))
                    .rules(Collections.EMPTY_LIST));
        }
        return oHGroupThinDtos;
    }

    public static OHGroupThinDto getRandomizedGroupThinDtoWithNameNotInList(List<OHGroupThinDto> oHGroupThinDtos) {
        List<String> usedNames = oHGroupThinDtos.stream().map(OHGroupThinDto::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(groupDescription);
        possibleNames.removeAll(usedNames);
        return new OHGroupThinDto()
                .name(getRandomFromArray(possibleNames))
                .rules(Collections.EMPTY_LIST);
    }

    public static List<OHGroupThinDto> getRandomLengthListOfOHGroupThinDto() {
        Random random = new Random();
        int numberOfGroupsThinDto = 1 + random.nextInt(3);
        List<OHGroupThinDto> groupsThinDto = new ArrayList<>();
        for (int i = 0; i <= numberOfGroupsThinDto; i++) {
            groupsThinDto.add(getRandomizedGroupThinDtoWithNameNotInList(groupsThinDto));
        }
        return groupsThinDto;
    }

    public static List<OHGroupThinDto> getNonEmptyListOfOHGroupThinDto(int length) {
        List<OHGroupThinDto> groupsThinDto = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            groupsThinDto.add(getRandomizedGroupThinDtoWithNameNotInList(groupsThinDto));
        }
        return groupsThinDto;
    }

    public static HelpTextDto getRandomizedHelpTextDto() {
        return new HelpTextDto()
                .number(getRandomNumberOfServiceType())
                .type(getRandomServiceTypeDto())
                .content(getRandomFromArray(helpTextDescriptions));
    }

    public static List<HelpTextDto> getRandomNumberOfHelpTextDtosOfServiceType() {
        List<HelpTextDto> helpTextDtos = new ArrayList<>();
        int numberOfServiceTypes = getRandomNumberOfServiceType();
            for (int i = 0; i < numberOfServiceTypes; i++) {
            helpTextDtos.add(new HelpTextDto()
                    .number(i+1)
                    .type(ServiceTypeDto.TJENESTE)
                    .content(getRandomFromArray(helpTextDescriptions)));
        }
        return helpTextDtos;
    }

    public static List<HelpTextDto> getRandomNumberOfHelpTextDtosOfKomponentType() {
        List<HelpTextDto> helpTextDtos = new ArrayList<>();
        int numberOfComponentTypes = getRandomNumberOfServiceType();
        for (int i = 0; i < numberOfComponentTypes; i++) {
            helpTextDtos.add(new HelpTextDto()
                    .number(i+1)
                    .type(ServiceTypeDto.KOMPONENT)
                    .content(getRandomFromArray(helpTextDescriptions)));
        }
        return helpTextDtos;
    }

    public static List<HelpTextDto> getRandomNumberOfHelpTextDtos() {
        List<HelpTextDto> result = Stream.concat(
                getRandomNumberOfHelpTextDtosOfServiceType().stream(),
                getRandomNumberOfHelpTextDtosOfKomponentType().stream())
                .collect(Collectors.toList());
        return result;
    }

    public static List<OHGroupThinDto> getListOfOHGroupThinDto() {
        List<OHGroupThinDto> oHGroupThinDtos = new ArrayList<>();
        for (String s : groupDescription) {
            oHGroupThinDtos.add(new OHGroupThinDto()
                    .name(s)
                    .rules(Collections.EMPTY_LIST));
        }
        return oHGroupThinDtos;
    }

    private static String getRandomFromKey(List<String> namesAndRulesKeys) {
        if (namesAndRulesKeys.size() == 0) {
            //Hit skal man ikke komme
            return null;
        }
        Random random = new Random();
        return namesAndRulesKeys.get(random.nextInt(namesAndRulesKeys.size()));
    }

    public static List<HelpTextDto> getNonEmptyListOfHelpTextDtos(int length) {
        List<HelpTextDto> helpTextDtos = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            helpTextDtos.add(new HelpTextDto()
                    .number(i+1)
                    .content(getRandomFromArray(helpTextDescriptions)));
        }
        return helpTextDtos;
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

    private static OffsetDateTime offsetNow(){
        return OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    }

    private static OPSmessageDto.SeverityEnum getRandomOpsMessageDtoSeverity() {
        Random random = new Random();
        return OPSmessageDto.SeverityEnum.values()[random.nextInt(OPSmessageDto.SeverityEnum.values().length)];
    }

    private static int getRandomNumberOfServiceType(){
        Random random = new Random();
        return random.nextInt(5) + 1;
    }
}
