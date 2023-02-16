package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.*;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.enums.ServiceType;
import no.portal.web.generated.api.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

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


    public static DashboardDto getRandomizedDashboardDto() {
        return new DashboardDto()
                .name(getRandomFromArray(dashboardNames));
    }

    public static List<DashboardDto> getDashboardDtos() {
        Random random = new Random();
        int numberOfDashboardDtos = random.nextInt(1, dashboardNames.size());
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
                .timestamp(OffsetDateTime.from(ZonedDateTime.now()))
                .status(getRandomStatusDto())
                .responseTime(getRandomResponseTime());
    }

    public static RecordDto getRandomizedRecordDtoForService(ServiceDto serviceDto) {
        return new RecordDto()
                .id(UUID.randomUUID())
                .serviceId(serviceDto.getId())
                .timestamp(OffsetDateTime.from(ZonedDateTime.now()))
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
                .startTime(OffsetDateTime.from(getZonedDateTimeNowWithOutDecimals()))
                .endTime(OffsetDateTime.from(getZonedDateTimeNowWithOutDecimals()).plusDays(2))
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
                .startTime(OffsetDateTime.from(getZonedDateTimeNowWithOutDecimals()).minusDays(startTime))
                .endTime(OffsetDateTime.from(getZonedDateTimeNowWithOutDecimals()).plusDays(endTime))
                .severity(getRandomOpsMessageDtoSeverity())
                .onlyShowForNavEmployees(random.nextBoolean())
                .isActive(true);
    }

    public static List<OPSmessageDto> getRandomLengthListOfOPSMessageDto() {
        Random random = new Random();
        int numberOfOpsMessageDto = 1 + random.nextInt(headersForOpsMessages.size());
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
