package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.*;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.enums.ServiceType;

import java.sql.Time;
import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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

    public static RecordEntity getRandomizedRecordEntity() {
        return new RecordEntity()
                .setCreated_at(ZonedDateTime.now())
                .setStatus(SampleData.getRandomServiceStatus())
                .setResponsetime(SampleData.getRandomResponseTime());
    }

    public static RecordEntity getRandomizedRecordStartingBeforeTimeSpan(LocalDateTime startInclusive) {
        //Generate an random amount from
        Random daysBack = new Random();
        int minusDays = daysBack.nextInt(10) + 1;
        return new RecordEntity()
                .setCreated_at(startInclusive.atZone(ZoneId.of("Europe/Oslo")).minusDays(minusDays))
                .setStatus(SampleData.getRandomServiceStatus())
                .setResponsetime(SampleData.getRandomResponseTime());
    }

    public static List<RecordEntity> generateRandomizedRecordEntities(ServiceEntity serviceEntity, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The argument amount refers to size of list, must be a non-zero, positive integer.");
        }
        List<RecordEntity> records = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            records.add(SampleData.getRandomizedRecordEntityForService(serviceEntity));
        }
        return records;
    }

    public static List<RecordEntity> generateRecordsEntitiesForTimeSpan
            (ServiceEntity serviceEntity, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        //Create random number of Zoned dates and times for period
        List<ZonedDateTime> zonedDateTimes = zonedDateTimes(startInclusive, endExclusive);

        //Create records and update with a zoned date and time
        List<RecordEntity> recordEntities = generateRandomizedRecordEntities(serviceEntity, zonedDateTimes.size());
        List<RecordEntity> recordEntitiesWithZonedDateTimes =
                updateRecordEntitiesWithZonedDateTimes(recordEntities, zonedDateTimes);

        //Create a record starting before the time period and add it as the first record in the records list
        recordEntitiesWithZonedDateTimes.addFirst(getRandomizedRecordStartingBeforeTimeSpan(startInclusive));

        /*Update the service status to follow the correct sequence of events: if a prior record is up
        the next record is down or the prior record is down the next record is up
        Thereafter return the updated record entities*/
        return updateRecordServiceStatus(recordEntitiesWithZonedDateTimes);
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

    public static List<ZonedDateTime> zonedDateTimes(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        //Generate a random amount from
        Random rand = new Random();
        int amount = rand.nextInt(5) + 1;

        List<ZonedDateTime> zonedDateTimes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            zonedDateTimes.add(getRandomLocalDateTime(startInclusive, endExclusive));
        }
        //sorts the dates consecutively, from earliest to latest
        Collections.sort(zonedDateTimes);
        return zonedDateTimes;
    }

    private static List<RecordEntity> updateRecordEntitiesWithZonedDateTimes
            (List<RecordEntity> recordEntities, List<ZonedDateTime> zonedDateTimes) {
        for (int i = 0; i < recordEntities.size(); i++) {
            recordEntities.get(i).setCreated_at(zonedDateTimes.get(i));
        }
        return recordEntities;
    }

    private static List<RecordEntity> updateRecordServiceStatus(List<RecordEntity> recordEntities) {
        if (recordEntities.size() > 1) {
            for (int i = 1; i < recordEntities.size(); i++) {
                if (recordEntities.get(i - 1).getStatus().equals(ServiceStatus.OK)) {
                    recordEntities.get(i).setStatus(ServiceStatus.DOWN);
                } else {
                    recordEntities.get(i).setStatus(ServiceStatus.OK);
                }
            }
        }
        return recordEntities;
    }

    public static ZonedDateTime getRandomLocalDateTime(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        LocalDate randomDate = getRandomDate(startInclusive.toLocalDate(),
                endExclusive.toLocalDate());
        LocalTime randomTime = getRandomTime();
        LocalDateTime localDateTime = LocalDateTime.of(randomDate, randomTime);
        return localDateTime.atZone(ZoneId.of("Europe/Oslo"));
    }

    private static LocalDate getRandomDate(LocalDate startInclusive, LocalDate endExclusive) {
        long startEpochDay = startInclusive.toEpochDay();
        long endEpochDay = endExclusive.toEpochDay();
        long randomDay = ThreadLocalRandom
                .current()
                .nextLong(startEpochDay, endEpochDay);

        return LocalDate.ofEpochDay(randomDay);
    }

    private static LocalTime getRandomTime() {
        Random generator = new Random();
        return LocalTime.MIN.plusSeconds(generator.nextLong());
    }
}
