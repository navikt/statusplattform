package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.*;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.enums.ServiceType;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class SampleData {

    static final ArrayList<String> areaNames = new ArrayList<>(Arrays.asList("Arbeidsøker eller permitert", "Pensjon", "Venter barn", "Alene med barn", "Sykmeldt",
            "Skal søke AAP", "Har mistet noen i nær famile", "Sykdom i familien", "Trenger tilrettelegging",
            "Trenger økonomisk sosialhjelp", "Trenger økonomisk rådgivning", "Berørt av EØS-saken", "Ett navn til", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> serviceNames = new ArrayList<>(Arrays.asList("Service1", "Testesrvice 2", "Æ er en tjeneste", "Øgletjeneste", "tjeneste123", "tjeneste213", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> urlStrings = new ArrayList<>(Arrays.asList("https://www.nrk.no", "https://www.nrk.no/nyheter/", "https://wwww.123abc.com", "https://wwww.ab.no", "https://wwww.ac.no", "https://wwww.ad.no", "https://wwww.ae.no", "https://wwww.af", "https://wwww.ag", "https://wwww.ah.no", "https://wwww.ai.no", "https://wwww.aj.no", "https://wwww.ak.no", "https://wwww.al.no", "https://wwww.am.no", "https://wwww.an.no", "https://wwww.ao.no"));

    static final ArrayList<String> teamNames = new ArrayList<>(Arrays.asList("team1", "teamOrlene", "teamÆØÅ", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> descriptions = new ArrayList<>(Arrays.asList("kort beskrivelse", "laaang beskrivelse ------- laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------", "beskrivelseÆØÅ"));

    static final ArrayList<String> icons = new ArrayList<>(Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010", "0011", "0012"));

    static final ArrayList<String> firstNames = new ArrayList<>(Arrays.asList("Arne", "Bodil", "Gudrun", "Kjell Åge", "Hufsa", "Elg", "Rake", "Æskild", "Øygunn"));
    static final ArrayList<String> maintenanceDescriptions = new ArrayList<>(Arrays.asList("Fixing the trøbbel i tårnet", "Supporting those som går gæli", "Mending the fuglekassa", "Taming memes", "Upkeep av nordavind fra alle kanter"));


    public static AreaEntity getRandomizedAreaEntity() {
        return new AreaEntity()
                .setName(getRandomFromArray(areaNames))
                .setDescription(getRandomFromArray(descriptions))
                .setContains_components(false);
    }

    public static AreaEntity getRandomizedAreaEntityWithNameNotInList(List<AreaEntity> areas) {
        List<String> usedNames = areas.stream().map(AreaEntity::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(areaNames);
        possibleNames.removeAll(usedNames);
        return new AreaEntity()
                .setName(getRandomFromArray(possibleNames))
                .setDescription(getRandomFromArray(descriptions))
                .setContains_components(false);
    }

    public static List<AreaEntity> getRandomLengthListOfAreaEntity() {
        int numberOfAreas = randomNonZeroPositiveInteger(12);
        List<AreaEntity> areas = new ArrayList<>();
        for (int i = 0; i <= numberOfAreas; i++) {
            areas.add(getRandomizedAreaEntityWithNameNotInList(areas));
        }
        return areas;
    }

    public static SubAreaEntity getRandomizedSubAreaEntity() {
        return new SubAreaEntity()
                .setName(getRandomFromArray(areaNames));
    }

    public static SubAreaEntity getRandomizedSubAreaEntityWithNameNotInList(List<SubAreaEntity> subAreas) {
        List<String> usedNames = subAreas.stream().map(SubAreaEntity::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(areaNames);
        possibleNames.removeAll(usedNames);
        return new SubAreaEntity()
                .setName(getRandomFromArray(possibleNames));
    }

    public static List<SubAreaEntity> getRandomLengthListOfSubAreaEntity() {
        int numberOfSubAreas = randomNonZeroPositiveInteger(12);
        List<SubAreaEntity> subAreas = new ArrayList<>();
        for (int i = 0; i <= numberOfSubAreas; i++) {
            subAreas.add(getRandomizedSubAreaEntityWithNameNotInList(subAreas));
        }
        return subAreas;
    }

    public static List<SubAreaEntity> getRandomLengthNonEmptyListOfSubAreaEntity() {
        int numberOfSubAreas = randomNonZeroPositiveInteger(12);
        List<SubAreaEntity> subAreas = new ArrayList<>();
        for (int i = 0; i <= numberOfSubAreas; i++) {
            subAreas.add(getRandomizedSubAreaEntityWithNameNotInList(subAreas));
        }
        return subAreas;
    }

    public static List<SubAreaEntity> getNonEmptyListOfSubAreaEntity(int length) {
        Random random = new Random();
        int numberOfSubAreas = length;
        List<SubAreaEntity> subAreas = new ArrayList<>();
        for (int i = 0; i < numberOfSubAreas; i++) {
            subAreas.add(getRandomizedSubAreaEntityWithNameNotInList(subAreas));
        }
        return subAreas;
    }

    public static ServiceEntity getRandomizedServiceEntity() {
        return new ServiceEntity()
                .setName(getRandomFromArray(serviceNames))
                .setType(getRandomServiceType())
                .setTeam(getRandomFromArray(teamNames))
                .setStatusNotFromTeam(Boolean.FALSE)
                .setPollingOnPrem(Boolean.FALSE)
                .setMonitorlink(getRandomFromArray(urlStrings));
    }

    public static ServiceEntity getRandomizedServiceEntityWithNameNotInList(List<ServiceEntity> services) {
        List<String> usedNames = services.stream().map(ServiceEntity::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = new ArrayList<>(serviceNames);
        possibleNames.removeAll(usedNames);
        return new ServiceEntity()
                .setName(getRandomFromArray(possibleNames))
                .setType(ServiceType.TJENESTE)
                .setTeam(getRandomFromArray(teamNames))
                .setPolling_url(getRandomFromArray(urlStrings))
                .setStatusNotFromTeam(Boolean.FALSE)
                .setPollingOnPrem(Boolean.FALSE)
                .setMonitorlink(getRandomFromArray(urlStrings));
    }

    public static List<ServiceEntity> getRandomLengthNonEmptyListOfServiceEntity() {
        int numberOfServices = randomNonZeroPositiveInteger(12);
        List<ServiceEntity> services = new ArrayList<>();
        for (int i = 0; i < numberOfServices; i++) {
            services.add(getRandomizedServiceEntityWithNameNotInList(services));
        }
        return services;
    }

    public static List<ServiceEntity> getNonEmptyListOfServiceEntity(int numberOfServices) {
        List<ServiceEntity> services = new ArrayList<>();
        for (int i = 0; i < numberOfServices; i++) {
            services.add(getRandomizedServiceEntityWithNameNotInList(services));
        }
        return services;
    }

    public static List<ServiceEntity> getNonEmptyListOfServiceEntityWithUid(int numberOfServices) {
        List<ServiceEntity> result = getNonEmptyListOfServiceEntity(numberOfServices);
        result.forEach(service -> service.setId(UUID.randomUUID()));
        return result;
    }

    public static <T> T getRandomFromArray(ArrayList<T> array) {
        if (array.isEmpty()) {
            //Hit skal man ikke komme
            throw new IllegalArgumentException("Expected input argument to contain a non-empty ArrayList");
        }
        Random random = new Random();
        return array.get(random.nextInt(array.size()));
    }

    static ServiceType getRandomServiceType() {
        ArrayList<ServiceType> serviceTypes = new ArrayList<>(EnumSet.allOf(ServiceType.class));
        return getRandomFromArray(serviceTypes);
    }

    public static ServiceStatus getRandomServiceStatus() {
        ArrayList<ServiceStatus> serviceStatuses = new ArrayList<>(EnumSet.allOf(ServiceStatus.class));
        return getRandomFromArray(serviceStatuses);
    }

    public static Integer getRandomResponseTime() {
        ArrayList<Integer> responseTime = new ArrayList<>(Arrays.asList(10, 20, 30, 100, 1000));
        return getRandomFromArray(responseTime);
    }

    public static RecordEntity getRandomizedRecordEntityForService(ServiceEntity serviceEntity) {
        return new RecordEntity()
                .setServiceId(serviceEntity.getId())
                .setCreated_at(ZonedDateTime.now())
                .setStatus(getRandomServiceStatus())
                .setResponsetime(getRandomResponseTime());
    }

    public static MaintenanceEntity getRandomizedMaintenanceEntity() {
        Random random = new Random();
        int numberOfDays = random.nextInt(2);
        return new MaintenanceEntity()
                .setCreated_at(ZonedDateTime.now())
                .setDescription(getRandomFromArray(maintenanceDescriptions))
                .setStart_time(ZonedDateTime.now().plusDays(numberOfDays))
                .setEnd_time(ZonedDateTime.now().plusDays(numberOfDays + 2));
    }

    public static int randomNonZeroPositiveInteger(int maxValue) {
        if (maxValue <= 0) {
            throw new IllegalArgumentException("Expected input argument to contain positive integers only");
        }
        return ensureNonZeroCheck(new Random().nextInt(maxValue), maxValue);
    }

    private static int ensureNonZeroCheck(int randomNumber, int maxValue) {
        if (randomNumber == 0) {
            return maxValue;
        }
        return randomNumber;
    }
}
