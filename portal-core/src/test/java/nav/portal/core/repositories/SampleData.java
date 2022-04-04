package nav.portal.core.repositories;

import nav.portal.core.entities.*;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.enums.ServiceType;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class SampleData {

    static ArrayList<String> areaNames = new ArrayList<>(Arrays.asList("Arbeidsøker eller permitert", "Pensjon", "Venter barn", "Alene med barn", "Sykmeldt",
            "Skal søke AAP", "Har mistet noen i nær famile", "Sykdom i familien", "Trenger tilrettelegging",
            "Trenger økonomisk sosialhjelp", "Trenger økonomisk rådgivning", "Berørt av EØS-saken","Ett navn til","ab","ac","ad","ae","af","ag","ah","ai","aj","ak","al","am","an","ao","ap","aq","ar","as","at"));

    static ArrayList<String> dashboardNames = new ArrayList<>(Arrays.asList("Privatperson", "Arbeidsgiver", "Sammarbeidspartner", "Et ganske så langt navn kommer her, går dette an da?", "ÆØÅ","ab","ac","ad","ae","af","ag","ah","ai","aj","ak","al","am","an","ao","ap","aq","ar","as","at"));

    static ArrayList<String> serviceNames = new ArrayList<>(Arrays.asList("Service1", "Testesrvice 2", "Æ er en tjeneste", "Øgletjeneste","tjeneste123","tjeneste213","ab","ac","ad","ae","af","ag","ah","ai","aj","ak","al","am","an","ao","ap","aq","ar","as","at"));

    static ArrayList<String> urlStrings = new ArrayList<>(Arrays.asList("www.random.com", "www.æøå.com", "wwww.123abc.com","ab","ac","ad","ae","af","ag","ah","ai","aj","ak","al","am","an","ao","ap","aq","ar","as","at"));

    static ArrayList<String> teamNames = new ArrayList<>(Arrays.asList("team1", "teamOrlene", "teamÆØÅ","ab","ac","ad","ae","af","ag","ah","ai","aj","ak","al","am","an","ao","ap","aq","ar","as","at"));

    static ArrayList<String> descriptions = new ArrayList<>(Arrays.asList("kort beskrivelse", "laaang beskrivelse ------- laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------laaang beskrivelse -------", "beskrivelseÆØÅ"));

    static ArrayList<String> icons = new ArrayList<>(Arrays.asList("0001", "0002", "0003", "0004", "0005", "0006", "0007", "0008", "0009", "0010", "0011", "0012"));


   public static String getRandomizedDashboardName() {
        return getRandomFromArray(dashboardNames);
    }

    public static AreaEntity getRandomizedAreaEntity() {
        return new AreaEntity()
                .setName(getRandomFromArray(areaNames))
                .setDescription(getRandomFromArray(descriptions))
                .setIcon(getRandomFromArray(icons));
    }

    public static AreaEntity getRandomizedAreaEntityWithNameNotInList(List<AreaEntity> areas) {
        List<String> usedNames = areas.stream().map(AreaEntity::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = areaNames;
        possibleNames.removeAll(usedNames);
        return new AreaEntity()
                .setName(getRandomFromArray(possibleNames))
                .setDescription(getRandomFromArray(descriptions))
                .setIcon(getRandomFromArray(icons));
    }

    public static List<AreaEntity> getRandomLengthListOfAreaEntity() {
        Random random = new Random();
        int numberOfAreas = random.nextInt(12);
        List<AreaEntity> areas = new ArrayList<>();
        for (int i = 0; i <= numberOfAreas; i++) {
            areas.add(getRandomizedAreaEntityWithNameNotInList(areas));
        }
        return areas;
    }

    public static List<AreaEntity> getRandomLengthNonEmptyListOfAreaEntity() {
        Random random = new Random();
        int numberOfAreas = 1 + random.nextInt(12);
        List<AreaEntity> areas = new ArrayList<>();
        for (int i = 0; i <= numberOfAreas; i++) {
            areas.add(getRandomizedAreaEntityWithNameNotInList(areas));
        }
        return areas;
    }

    public static List<AreaEntity> getNonEmptyListOfAreaEntity(int length) {
        Random random = new Random();
        int numberOfAreas = length;
        List<AreaEntity> areas = new ArrayList<>();
        for (int i = 0; i < numberOfAreas; i++) {
            areas.add(getRandomizedAreaEntityWithNameNotInList(areas));
        }
        return areas;
    }

    public static ServiceEntity getRandomizedServiceEntity() {
        return new ServiceEntity()
                .setName(getRandomFromArray(serviceNames))
                .setType(getRandomServiceType())
                .setTeam(getRandomFromArray(teamNames))
                .setMonitorlink(getRandomFromArray(urlStrings));
    }

    public static ServiceEntity getRandomizedServiceEntityWithNameNotInList(List<ServiceEntity> services) {
        List<String> usedNames = services.stream().map(ServiceEntity::getName).collect(Collectors.toList());
        ArrayList<String> possibleNames = serviceNames;
        possibleNames.removeAll(usedNames);
        return new ServiceEntity()
                .setName(getRandomFromArray(possibleNames))
                .setType(ServiceType.TJENESTE)
                .setTeam(getRandomFromArray(teamNames))
                .setMonitorlink(getRandomFromArray(urlStrings));
    }

    public static List<ServiceEntity> getRandomLengthListOfServiceEntity() {
        Random random = new Random();
        int numberOfAreas = random.nextInt(12);
        List<ServiceEntity> services = new ArrayList<>();
        for (int i = 0; i < numberOfAreas; i++) {
            services.add(getRandomizedServiceEntityWithNameNotInList(services));
        }
        return services;    }

    public static List<ServiceEntity> getRandomLengthNonEmptyListOfServiceEntity() {
        Random random = new Random();
        int numberOfServices = 1 + random.nextInt(12);
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
        List<ServiceEntity> result = getNonEmptyListOfServiceEntity( numberOfServices);
        result.forEach(service -> service.setId(UUID.randomUUID()));
        return result;
    }


    private static String getRandomFromArray(ArrayList<String> array) {
        Random random = new Random();
        return array.get(random.nextInt(array.size()));
    }

    private static ServiceType getRandomServiceType() {
        Random random = new Random();
        return ServiceType.values()[random.nextInt(ServiceType.values().length)];
    }

    public static List<String> getDashboardNames() {
        return dashboardNames;
    }

    private static ServiceStatus getRandomServiceStatus() {
        Random random = new Random();
        return ServiceStatus.values()[random.nextInt(ServiceStatus.values().length)];
    }

    private static Integer getRandomResponseTime() {
        List<Integer> responseTime = Arrays.asList(10, 20, 30, 100, 1000);
        Random random = new Random();
        return responseTime.get(random.nextInt(responseTime.size()));
    }

    public static RecordEntity getRandomizedRecordEntity() {
           return new RecordEntity()
                .setStatus(getRandomServiceStatus())
                .setResponsetime(getRandomResponseTime());
    }

    public static RecordEntity getRandomizedRecordEntityForService(ServiceEntity serviceEntity){
       return new RecordEntity()
               .setServiceId(serviceEntity.getId())
               .setStatus(getRandomServiceStatus())
               .setResponsetime(getRandomResponseTime());
    }

    public static DailyStatusAggregationForServiceEntity getRandomizedDailyStatusAggregationForService(ServiceEntity serviceEntity){
        return new DailyStatusAggregationForServiceEntity()
                .setService_id(serviceEntity.getId())
                .setNumber_of_status_down(new Random().nextInt(3))
                .setNumber_of_status_issue(new Random().nextInt(2))
                .setNumber_of_status_ok(new Random().nextInt(100))
                .setAggregation_date(LocalDate.now());
    }
}
