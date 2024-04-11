package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.OpeningHoursGroup;
import nav.statusplattform.core.entities.OpeningHoursGroupEntity;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceType;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.*;

public class OpeningHoursRepositoryTestV2 {
    private final ArrayList<String> areaNames = new ArrayList<>(Arrays.asList("Arbeidsøker eller permitert", "Pensjon", "Venter barn", "Alene med barn", "Sykmeldt",
            "Skal søke AAP", "Har mistet noen i nær famile", "Sykdom i familien", "Trenger tilrettelegging",
            "Trenger økonomisk sosialhjelp", "Trenger økonomisk rådgivning", "Berørt av EØS-saken", "Ett navn til", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    private final ArrayList<String> serviceNames = new ArrayList<>(Arrays.asList("Service1", "Testesrvice 2", "Æ er en tjeneste", "Øgletjeneste", "tjeneste123", "tjeneste213", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    private final ArrayList<String> teamNames = new ArrayList<>(Arrays.asList("team1", "teamOrlene", "teamÆØÅ", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    static final ArrayList<String> urlStrings = new ArrayList<>(Arrays.asList("https://www.nrk.no", "https://www.nrk.no/nyheter/", "https://wwww.123abc.com", "https://wwww.ab.no", "https://wwww.ac.no", "https://wwww.ad.no", "https://wwww.ae.no", "https://wwww.af", "https://wwww.ag", "https://wwww.ah.no", "https://wwww.ai.no", "https://wwww.aj.no", "https://wwww.ak.no", "https://wwww.al.no", "https://wwww.am.no", "https://wwww.an.no", "https://wwww.ao.no"));

    private final ArrayList<String> groupDescription = new ArrayList<>(Arrays.asList("Local maintenance", "Collaborative maintenance", "Early closing", "National Holidays"));

    private final ArrayList<String> rules = new ArrayList<>(Arrays.asList("06.04.2023 ? ? 00:00-00:00","??.??.???? 1-5,10-L ? 07:00-21:00","24.12.???? ? 1-5 09:00-14:00"));

    private final Map<String, String> namesAndRules =  Map.ofEntries(
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

    private final DataSource dataSource = TestDataSource.create();

    private final DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    private final OpeningHoursRepository openingHoursRepository = new OpeningHoursRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

     private String getRandomFromArray(ArrayList<String> array) {
         //Hit skal man ikke komme
         if (array.isEmpty()) return null;
        Random random = new Random();
        return array.get(random.nextInt(array.size()));
    }

    private OpeningHoursRuleEntity getRandomizedOpeningHoursRuleEntityWithNameNotInList(List<OpeningHoursRuleEntity> OpeningHoursRules) {
        List<String> usedNames = OpeningHoursRules.stream().map(OpeningHoursRuleEntity::getName).toList();
        ArrayList<String> possibleNames = new ArrayList<>(areaNames);
        possibleNames.removeAll(usedNames);
        return new OpeningHoursRuleEntity()
                .setName(getRandomFromArray(possibleNames))
                .setRule(getRandomFromArray(rules));
    }

    private OpeningHoursGroupEntity getRandomizedGroupEntitiesWithNameNotInList(List<OpeningHoursGroupEntity> openingHoursGroupEntities) {
        List<String> usedNames = openingHoursGroupEntities.stream().map(OpeningHoursGroupEntity::getName).toList();
        ArrayList<String> possibleNames = new ArrayList<>(groupDescription);
        possibleNames.removeAll(usedNames);
        return new OpeningHoursGroupEntity()
                .setName(getRandomFromArray(possibleNames))
                .setRules(Collections.EMPTY_LIST);
    }

    private OpeningHoursRuleEntity getRandomizedOHRuleEntityWithNameNotInList(List<OpeningHoursRuleEntity>openingHoursRuleEntities) {
        List<String> usedNames = openingHoursRuleEntities.stream().map(OpeningHoursRuleEntity::getName).toList();
        ArrayList<String> possibleNames = new ArrayList<>(namesAndRules.keySet());
        possibleNames.removeAll(usedNames);
        String randomKey = getRandomFromKey(new ArrayList<>(possibleNames));
        return new OpeningHoursRuleEntity()
                .setName(randomKey)
                .setRule(namesAndRules.get(randomKey));
    }

    private String getRandomFromKey(List<String> namesAndRulesKeys) {
        if (namesAndRulesKeys.isEmpty()) {
            //Hit skal man ikke komme
            return null;
        }
        Random random = new Random();
        return namesAndRulesKeys.get(random.nextInt(namesAndRulesKeys.size()));
    }

    private ServiceType getRandomServiceType() {
        Random random = new Random();
        return ServiceType.values()[random.nextInt(ServiceType.values().length)];
    }

    private ServiceEntity getRandomizedServiceEntity() {
        return new ServiceEntity()
                .setName(getRandomFromArray(serviceNames))
                .setType(getRandomServiceType())
                .setTeam(getRandomFromArray(teamNames))
                .setStatusNotFromTeam(Boolean.FALSE)
                .setPollingOnPrem(Boolean.FALSE)
                .setMonitorlink(getRandomFromArray(urlStrings));
    }

    private OpeningHoursRuleEntity getRandomizedOpeningRule() {
        return new OpeningHoursRuleEntity()
                .setName(getRandomFromArray(areaNames))
                .setRule(getRandomFromArray(rules));
    }

    private List<OpeningHoursRuleEntity> getNonEmptyListOfOpeningRules() {
        int numberOfRules = 2;
        List<OpeningHoursRuleEntity> OpeningHoursRules = new ArrayList<>();
        for (int i = 0; i < numberOfRules; i++) {
            OpeningHoursRules.add(getRandomizedOpeningHoursRuleEntityWithNameNotInList(OpeningHoursRules));
        }
        return OpeningHoursRules;
    }

    private List<OpeningHoursGroupEntity> getNonEmptyListOfOpeningHoursGroupEntities() {
        List<OpeningHoursGroupEntity> groupEntities = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            groupEntities.add(getRandomizedGroupEntitiesWithNameNotInList(groupEntities));
        }
        return groupEntities;
    }

    private List<OpeningHoursRuleEntity> getRandomLengthListOfOHRuleEntity() {
        Random random = new Random();
        int numberOfOHRulesEntities = 1 + random.nextInt(4);
        List<OpeningHoursRuleEntity> oHRuleEntities = new ArrayList<>();
        for (int i = 0; i <= numberOfOHRulesEntities; i++) {
            oHRuleEntities.add(getRandomizedOHRuleEntityWithNameNotInList(oHRuleEntities));
        }
        return oHRuleEntities;
    }

    @Test
    void save() {
        //Arrange
        OpeningHoursRuleEntity rule = getRandomizedOpeningRule();
        //Act
        UUID id = openingHoursRepository.save(rule);
        //Assert
        Assertions.assertThat(id).isNotNull();
    }

    @Test
    void update() {
        //Arrange
        List<OpeningHoursRuleEntity> rules = getNonEmptyListOfOpeningRules();
        rules.forEach(rule -> rule.setId(openingHoursRepository.save(rule)));
        UUID ruleId = rules.getFirst().getId();
        Optional<OpeningHoursRuleEntity> before = openingHoursRepository.retriveRule(ruleId);
        OpeningHoursRuleEntity retrievedBefore = before.orElse(null);
        //Act
        rules.get(0).setName(rules.get(1).getName());
        rules.get(0).setRule(rules.get(1).getRule());
        rules.get(0).setId(rules.get(1).getId());
        openingHoursRepository.update(rules.get(0));
        Optional<OpeningHoursRuleEntity> after = openingHoursRepository.retriveRule(rules.get(0).getId());
        OpeningHoursRuleEntity retrievedAfter = after.orElse(null);
        //Assert
        assert retrievedBefore != null;
        Assertions.assertThat(retrievedBefore.getName()).isNotEqualToIgnoringCase(rules.get(0).getName());
        assert retrievedAfter != null;
        Assertions.assertThat(retrievedAfter.getName()).isEqualTo(rules.get(1).getName());
    }

    @Test
    void deleteOpeninghours() {
        //Assign
        OpeningHoursRuleEntity ruleToBeDeleted = getRandomizedOpeningRule();
        ruleToBeDeleted.setId(openingHoursRepository.save(ruleToBeDeleted));
        Optional<OpeningHoursRuleEntity> beforeDelete = openingHoursRepository.retriveRule(ruleToBeDeleted.getId());
        //Act
        boolean isDeleted = openingHoursRepository.deleteOpeningHoursRule(ruleToBeDeleted.getId());
        Optional<OpeningHoursRuleEntity> afterDelete = openingHoursRepository.retriveRule(ruleToBeDeleted.getId());
        //Assert
        Assertions.assertThat(beforeDelete).contains(ruleToBeDeleted);
        Assertions.assertThat(isDeleted).isTrue();
        Assertions.assertThat(afterDelete).isEmpty();
    }

    @Test
    void saveGroup() {
        //Arrange
        OpeningHoursRuleEntity rule = getRandomizedOpeningRule();
        UUID rule_id = openingHoursRepository.save(rule);
        rule.setId(rule_id);
        OpeningHoursGroupEntity group = new OpeningHoursGroupEntity().setName("Ny gruppe").setRules(List.of(rule_id));
        //Act
        UUID group_id = openingHoursRepository.saveGroup(group);
        group.setId(group_id);
        Optional<OpeningHoursGroup> retrievedGroup = openingHoursRepository.retrieveOneGroup(group_id);
        //Assert
        Assertions.assertThat(retrievedGroup).isPresent();
        Assertions.assertThat(retrievedGroup.get().getRules()).contains(rule);
        Assertions.assertThat(retrievedGroup.get().getId()).isEqualTo(group.getId());
    }

    @Test
    void updateGroup() {
        //Arrange
        List<OpeningHoursGroupEntity>openingHoursGroupEntities = getNonEmptyListOfOpeningHoursGroupEntities();
        List<OpeningHoursGroupEntity>groupEntities = new ArrayList<>();
        openingHoursGroupEntities.forEach(openingHoursGroupEntity -> groupEntities.add(openingHoursGroupEntity.setId(openingHoursRepository.saveGroup(openingHoursGroupEntity))));
        List<OpeningHoursGroup>retrievedGroupsBefore = openingHoursRepository.getAllGroups();
        //Act
        groupEntities.get(0).setName(groupEntities.get(1).getName());
        openingHoursRepository.updateGroup(groupEntities.get(0));
        List<OpeningHoursGroup>retrievedGroupsAfter = openingHoursRepository.getAllGroups();
        //Assert
        Assertions.assertThat(retrievedGroupsBefore.get(0).getName()).isNotEqualTo(retrievedGroupsBefore.get(1).getName());
        Assertions.assertThat(retrievedGroupsAfter.get(0).getName()).isEqualTo(retrievedGroupsAfter.get(1).getName());
    }

    @Test
    void retrieveRule() {
        //Arrange
        List<OpeningHoursRuleEntity> rules = getNonEmptyListOfOpeningRules();
        rules.forEach(rule -> {
            rule.setId(openingHoursRepository.save(rule));
        });
        OpeningHoursRuleEntity ruleForRetrieving = rules.get(0);
        //Act
        Optional<OpeningHoursRuleEntity> retrievedRule = openingHoursRepository.retriveRule(ruleForRetrieving.getId());
        //Assert
        Assertions.assertThat(retrievedRule).contains(ruleForRetrieving);
    }

    @Test
    void getAllOpeningHoursRules(){
        //Arrange
        List<OpeningHoursRuleEntity> rules = getRandomLengthListOfOHRuleEntity();
        List<UUID>rulesId = new ArrayList<>();
        rules.forEach(rule -> {
            rule.setId(openingHoursRepository.save(rule));
            rulesId.add(rule.getId());
        });
        List<OpeningHoursRuleEntity> retrievedRulesBefore = new ArrayList<>();
        rulesId.forEach(ruleId ->{
            Optional<OpeningHoursRuleEntity> before = openingHoursRepository.retriveRule(ruleId);
            retrievedRulesBefore.add(before.orElse(null));
        });
        //Act
        List<OpeningHoursRuleEntity> retrievedRulesAfter = openingHoursRepository.getAllOpeningHoursRules();
        //Assert
        Assertions.assertThat(retrievedRulesAfter).containsAll(retrievedRulesBefore);
        Assertions.assertThat(retrievedRulesAfter.size()).isEqualTo(retrievedRulesBefore.size());
    }

    @Test
    void deleteOpeninghourGroup() {
        //Arrange
        OpeningHoursRuleEntity rule = getRandomizedOpeningRule();
        UUID rule_id = openingHoursRepository.save(rule);
        rule.setId(rule_id);
        OpeningHoursGroupEntity group = new OpeningHoursGroupEntity().setName("Ny gruppe").setRules(List.of(rule_id));
        UUID group_id = openingHoursRepository.saveGroup(group);
        group.setId(group_id);
        //Act
        boolean isDeleted = openingHoursRepository.deleteOpeninghourGroup(group_id);
        Optional<OpeningHoursGroup> retrievedGroupAfter = openingHoursRepository.retrieveOneGroup(group_id);
        //Assert
        Assertions.assertThat(isDeleted).isTrue();
        Assertions.assertThat(retrievedGroupAfter).isEmpty( );
    }

    @Test
    void retrieveOneGroupSimple() {
        //Arrange
        OpeningHoursRuleEntity rule = getRandomizedOpeningRule();
        UUID rule_id = openingHoursRepository.save(rule);
        rule.setId(rule_id);
        OpeningHoursGroupEntity group = new OpeningHoursGroupEntity().setName("Ny gruppe").setRules(List.of(rule_id));
        UUID group_id = openingHoursRepository.saveGroup(group);
        //Act
        Optional<OpeningHoursGroup> retrievedGroup = openingHoursRepository.retrieveOneGroup(group_id);
        //Assert
        Assertions.assertThat(retrievedGroup).isPresent();
        Assertions.assertThat(retrievedGroup.get().getRules()).containsExactly(rule);
    }

    @Test
    void retrieveAllGroupsForAllServicesSimple() {
        //Arrange
        OpeningHoursRuleEntity rule = getRandomizedOpeningRule();
        UUID rule_id = openingHoursRepository.save(rule);
        rule.setId(rule_id);
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(serviceEntity);
        serviceEntity.setId(serviceId);
        OpeningHoursGroupEntity group = new OpeningHoursGroupEntity().setName("Ny gruppe").setRules(List.of(rule_id));
        UUID groupId = openingHoursRepository.saveGroup(group);
        openingHoursRepository.setOpeningHoursToService(groupId,serviceId);
        //Act
        Map<UUID,OpeningHoursGroup> serviceGroupMap = openingHoursRepository.getAllOpeningtimeForAllServicesWithOpeningTime();
        //Assert
        serviceGroupMap.get(UUID.randomUUID());
    }





}
