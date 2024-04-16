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
import java.util.stream.Collectors;

class OpeningHoursRepositoryTest {

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

    private final ArrayList<String> areaNames = new ArrayList<>(Arrays.asList("Arbeidsøker eller permitert", "Pensjon", "Venter barn", "Alene med barn", "Sykmeldt",
            "Skal søke AAP", "Har mistet noen i nær famile", "Sykdom i familien", "Trenger tilrettelegging",
            "Trenger økonomisk sosialhjelp", "Trenger økonomisk rådgivning", "Berørt av EØS-saken", "Ett navn til", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    private final ArrayList<String> serviceNames = new ArrayList<>(Arrays.asList("Service1", "Testesrvice 2", "Æ er en tjeneste", "Øgletjeneste", "tjeneste123", "tjeneste213", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    private final ArrayList<String> urlStrings = new ArrayList<>(Arrays.asList("https://www.nrk.no", "https://www.nrk.no/nyheter/", "https://wwww.123abc.com", "https://wwww.ab.no", "https://wwww.ac.no", "https://wwww.ad.no", "https://wwww.ae.no", "https://wwww.af", "https://wwww.ag", "https://wwww.ah.no", "https://wwww.ai.no", "https://wwww.aj.no", "https://wwww.ak.no", "https://wwww.al.no", "https://wwww.am.no", "https://wwww.an.no", "https://wwww.ao.no"));

    private final ArrayList<String> teamNames = new ArrayList<>(Arrays.asList("team1", "teamOrlene", "teamÆØÅ", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    private final ArrayList<String> rules = new ArrayList<>(Arrays.asList("06.04.2023 ? ? 00:00-00:00","??.??.???? 1-5,10-L ? 07:00-21:00","24.12.???? ? 1-5 09:00-14:00"));

    static final ArrayList<String> groupDescription = new ArrayList<>(Arrays.asList("Local maintenance", "Collaborative maintenance", "Early closing", "National Holidays"));

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
    void deleteOpeningHours() {
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
        List<OpeningHoursRuleEntity> rules = getNonEmptyListOfOpeningRules(2);
        for (OpeningHoursRuleEntity rule : rules) {
            rule.setId(openingHoursRepository.save(rule));
        }
        OpeningHoursRuleEntity ruleForRetrieving = rules.getFirst();
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
        List<OpeningHoursRuleEntity> retrievedRulesBefore = rulesId.stream().map(openingHoursRepository::retriveRule).map(before -> before.orElse(null)).collect(Collectors.toList());
        //Act
        List<OpeningHoursRuleEntity> retrievedRulesAfter = openingHoursRepository.getAllOpeningHoursRules();
        //Assert
        Assertions.assertThat(retrievedRulesAfter).containsAll(retrievedRulesBefore);
        Assertions.assertThat(retrievedRulesAfter.size()).isEqualTo(retrievedRulesBefore.size());
    }

    @Test
    void getAllOpeningHoursGroups(){
        //Arrange
        List<OpeningHoursGroupEntity>groups= getRandomLengthListOfOpeningHoursGroupEntities();
        List<UUID>groupsId = new ArrayList<>();
        groups.forEach(group -> {
            group.setId(openingHoursRepository.saveGroup(group));
            groupsId.add(group.getId());
        });
        List<OpeningHoursGroup> retrievedGroupsBefore = new ArrayList<>();
        groupsId.forEach(groupId ->{
            Optional<OpeningHoursGroup> before = openingHoursRepository.retrieveOneGroup(groupId);
            retrievedGroupsBefore.add(before.orElse(null));
        });
        //Act
        List<OpeningHoursGroup> retrievedGroupsAfter = openingHoursRepository.getAllGroups();
        //Assert
        Assertions.assertThat(retrievedGroupsAfter).containsAll(retrievedGroupsBefore);
        Assertions.assertThat(retrievedGroupsAfter.size()).isEqualTo(retrievedGroupsBefore.size());
    }

    @Test
    void deleteOpeningHourGroup() {
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
        ServiceEntity serviceEntity = getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(serviceEntity);
        serviceEntity.setId(serviceId);
        OpeningHoursGroupEntity group = new OpeningHoursGroupEntity().setName("Ny gruppe").setRules(List.of(rule_id));
        UUID groupId = openingHoursRepository.saveGroup(group);
        openingHoursRepository.setOpeningHoursToService(groupId,serviceId);
        //Act
        Map<UUID,OpeningHoursGroup> ohServiceMap = openingHoursRepository.getAllOpeningtimeForAllServicesWithOpeningTime();
        //Assert
        Set<UUID> ohServiceGroupMapKey = ohServiceMap.keySet();
        UUID ohServiceGroupAfterId = ohServiceGroupMapKey.stream().findFirst().orElse(null);
        OpeningHoursGroup ohServiceGroupAfter = ohServiceMap.get(ohServiceGroupAfterId);

        Assertions.assertThat(ohServiceGroupMapKey.size()).isEqualTo(1);
        Assertions.assertThat(ohServiceGroupAfter.getRules()).contains(rule);
        Assertions.assertThat(ohServiceGroupAfterId).isEqualTo(serviceId);
    }

    @Test
    void retrieveOneGroupComplex() {

        List<OpeningHoursRuleEntity> rules = getNonEmptyListOfOpeningRules(6);
        //Arrange
        OpeningHoursRuleEntity rule1 = rules.get(0);
        OpeningHoursRuleEntity rule2 = rules.get(1);
        OpeningHoursRuleEntity rule3 = rules.get(2);
        OpeningHoursRuleEntity rule4 = rules.get(3);
        OpeningHoursRuleEntity rule5 = rules.get(4);
        OpeningHoursRuleEntity rule6 = rules.get(5);
    //TODO SampleData.getRandomizedOpeningRule() må  gi unikt navn hver gang
        UUID rule1_id = openingHoursRepository.save(rule1);
        UUID rule2_id = openingHoursRepository.save(rule2);
        UUID rule3_id = openingHoursRepository.save(rule3);
        UUID rule4_id = openingHoursRepository.save(rule4);
        UUID rule5_id = openingHoursRepository.save(rule5);
        UUID rule6_id = openingHoursRepository.save(rule6);
        rule1.setId(rule1_id);
        rule2.setId(rule2_id);
        rule3.setId(rule3_id);
        rule4.setId(rule4_id);
        rule5.setId(rule5_id);
        rule6.setId(rule6_id);
        //g3
        OpeningHoursGroupEntity group3 = new OpeningHoursGroupEntity().setName("Gruppe3").setRules(List.of(rule2_id,rule3_id));
        group3.setId(openingHoursRepository.saveGroup(group3));
        //g4
        OpeningHoursGroupEntity group4 = new OpeningHoursGroupEntity().setName("Gruppe4").setRules(List.of(rule4_id,rule5_id));
        group4.setId(openingHoursRepository.saveGroup(group4));
        //g2
        OpeningHoursGroupEntity group2 = new OpeningHoursGroupEntity().setName("Gruppe2").setRules(List.of(group3.getId(),group4.getId()));
        group2.setId(openingHoursRepository.saveGroup(group2));

        //g1
        OpeningHoursGroupEntity group1 = new OpeningHoursGroupEntity().setName("Gruppe1").setRules(List.of(rule1_id,group2.getId(),rule6_id));
        group1.setId(openingHoursRepository.saveGroup(group1));

        //Act
        Optional<OpeningHoursGroup> retrievedGroup = openingHoursRepository.retrieveOneGroup(group1.getId());
        //Assert
        Assertions.assertThat(retrievedGroup).isPresent();
//        Assertions.assertThat(retrievedGroup.get().getRules()).containsExactlyElementsOf(rulesOfGroup2);
    }

    @Test
    void retrieveOneGroupComplexForAllServicesWithGroups() {

        List<OpeningHoursRuleEntity> rules = getNonEmptyListOfOpeningRules(6);
        //Arrange

        // Setting up rules:
        OpeningHoursRuleEntity rule1 = rules.get(0);
        OpeningHoursRuleEntity rule2 = rules.get(1);
        OpeningHoursRuleEntity rule3 = rules.get(2);
        OpeningHoursRuleEntity rule4 = rules.get(3);
        OpeningHoursRuleEntity rule5 = rules.get(4);
        OpeningHoursRuleEntity rule6 = rules.get(5);
        //TODO SampleData.getRandomizedOpeningRule() må  gi unikt navn hver gang
        UUID rule1_id = openingHoursRepository.save(rule1);
        UUID rule2_id = openingHoursRepository.save(rule2);
        UUID rule3_id = openingHoursRepository.save(rule3);
        UUID rule4_id = openingHoursRepository.save(rule4);
        UUID rule5_id = openingHoursRepository.save(rule5);
        UUID rule6_id = openingHoursRepository.save(rule6);
        rule1.setId(rule1_id);
        rule2.setId(rule2_id);
        rule3.setId(rule3_id);
        rule4.setId(rule4_id);
        rule5.setId(rule5_id);
        rule6.setId(rule6_id);
        //g3
        // Setting up groups:
        OpeningHoursGroupEntity group3 = new OpeningHoursGroupEntity().setName("Gruppe3").setRules(List.of(rule2_id,rule3_id));
        group3.setId(openingHoursRepository.saveGroup(group3));
        //g4
        OpeningHoursGroupEntity group4 = new OpeningHoursGroupEntity().setName("Gruppe4").setRules(List.of(rule4_id,rule5_id));
        group4.setId(openingHoursRepository.saveGroup(group4));
        //g2
        OpeningHoursGroupEntity group2 = new OpeningHoursGroupEntity().setName("Gruppe2").setRules(List.of(group3.getId(),group4.getId()));
        group2.setId(openingHoursRepository.saveGroup(group2));

        //g1
        OpeningHoursGroupEntity group1 = new OpeningHoursGroupEntity().setName("Gruppe1").setRules(List.of(rule1_id,group2.getId(),rule6_id));
        group1.setId(openingHoursRepository.saveGroup(group1));

        // Setting up services
        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(4);
        ServiceEntity service1 = services.get(0);
        UUID serviceId1 = service1.setId(serviceRepository.save(service1)).getId();

        ServiceEntity service2 = services.get(1);
        UUID serviceId2 = service2.setId(serviceRepository.save(service2)).getId();

        ServiceEntity service3 = services.get(2);
        UUID serviceId3 = service3.setId(serviceRepository.save(service3)).getId();

        ServiceEntity service4 = services.get(3);
        UUID serviceId4 = service4.setId(serviceRepository.save(service4)).getId();


        //Adding groups to services
        openingHoursRepository.setOpeningHoursToService(group1.getId(),service1.getId());

        openingHoursRepository.setOpeningHoursToService(group2.getId(),service2.getId());

        openingHoursRepository.setOpeningHoursToService(group3.getId(),service3.getId());

        openingHoursRepository.setOpeningHoursToService(group4.getId(),service4.getId());
        //Act
        Map<UUID,OpeningHoursGroup> serviceIdGroupMap = openingHoursRepository.getAllOpeningtimeForAllServicesWithOpeningTime();
        //Assert
        Assertions.assertThat(serviceIdGroupMap.size()).isEqualTo(4);
        Assertions.assertThat(serviceIdGroupMap.keySet()).containsExactlyInAnyOrder(serviceId1,serviceId2,serviceId3,serviceId4);
        Assertions.assertThat(serviceIdGroupMap.get(serviceId1).getId()).isEqualTo(group1.getId());
        Assertions.assertThat(serviceIdGroupMap.get(serviceId2).getId()).isEqualTo(group2.getId());
        Assertions.assertThat(serviceIdGroupMap.get(serviceId3).getId()).isEqualTo(group3.getId());
        Assertions.assertThat(serviceIdGroupMap.get(serviceId4).getId()).isEqualTo(group4.getId());

    }

    @Test
    void setOpeningHoursGroupToService() {
        //Arrange
        /*Create service*/
        ServiceEntity service = getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        /*Create group*/
        OpeningHoursGroupEntity group = getRandomizedOpeningHoursGroupEntity();
        group.setId(openingHoursRepository.saveGroup(group));
        UUID groupId = group.getId();
        //Act
        openingHoursRepository.setOpeningHoursToService(groupId, serviceId);
        //Assert
        Optional<OpeningHoursGroup>retrievedGroup = openingHoursRepository.getOHGroupForService(serviceId);
        Assertions.assertThat(retrievedGroup).isPresent();
        Assertions.assertThat(retrievedGroup.get().getId()).isEqualTo(group.getId());
    }

    @Test
    void removeOpeningHoursFromService() {
        //Arrange
        /*Create service*/
        ServiceEntity service = getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        /*Create group*/
        OpeningHoursGroupEntity group = getRandomizedOpeningHoursGroupEntity();
        group.setId(openingHoursRepository.saveGroup(group));
        UUID groupId = group.getId();
        openingHoursRepository.setOpeningHoursToService(groupId, serviceId);
        Optional<OpeningHoursGroup>retrievedGroupBefore = openingHoursRepository.getOHGroupForService(serviceId);
        //Act
        openingHoursRepository.removeOpeningHoursFromService(serviceId);
        Optional<OpeningHoursGroup>retrievedGroupAfter = openingHoursRepository.getOHGroupForService(serviceId);
        //Assert
        Assertions.assertThat(retrievedGroupBefore).isPresent();
        Assertions.assertThat(retrievedGroupBefore.get().getId()).isEqualTo(group.getId());
        Assertions.assertThat(retrievedGroupAfter).isEmpty();
    }

    @Test
    void getOHGroupForService(){
        //Arrange
        /*Create service*/
        ServiceEntity service = getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        /*Create group*/
        OpeningHoursGroupEntity group = getRandomizedOpeningHoursGroupEntity();
        group.setId(openingHoursRepository.saveGroup(group));
        UUID groupId = group.getId();
        openingHoursRepository.setOpeningHoursToService(groupId, serviceId);
        //Act
        Optional<OpeningHoursGroup>retrievedGroup = openingHoursRepository.getOHGroupForService(serviceId);
        //Assert
        Assertions.assertThat(retrievedGroup).isPresent();
        Assertions.assertThat(retrievedGroup.get().getId()).isEqualTo(group.getId());
    }

    private OpeningHoursRuleEntity getRandomizedOpeningRule() {
        return new OpeningHoursRuleEntity()
                .setName(getRandomFromArray(areaNames))
                .setRule(getRandomFromArray(rules));
    }

    private String getRandomFromArray(ArrayList<String> array) {
        //Hit skal man ikke komme
        if (array.isEmpty()) return null;
        Random random = new Random();
        return array.get(random.nextInt(array.size()));
    }

    private List<OpeningHoursRuleEntity> getNonEmptyListOfOpeningRules() {
        int numberOfRules = 2;
        List<OpeningHoursRuleEntity> OpeningHoursRules = new ArrayList<>();
        for (int i = 0; i < numberOfRules; i++) {
            OpeningHoursRules.add(getRandomizedOpeningHoursRuleEntityWithNameNotInList(OpeningHoursRules));
        }
        return OpeningHoursRules;
    }

    private OpeningHoursRuleEntity getRandomizedOpeningHoursRuleEntityWithNameNotInList(List<OpeningHoursRuleEntity> OpeningHoursRules) {
        List<String> usedNames = OpeningHoursRules.stream().map(OpeningHoursRuleEntity::getName).toList();
        ArrayList<String> possibleNames = new ArrayList<>(areaNames);
        possibleNames.removeAll(usedNames);
        return new OpeningHoursRuleEntity()
                .setName(getRandomFromArray(possibleNames))
                .setRule(getRandomFromArray(rules));
    }

    private List<OpeningHoursGroupEntity> getNonEmptyListOfOpeningHoursGroupEntities() {
        List<OpeningHoursGroupEntity> groupEntities = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            groupEntities.add(getRandomizedGroupEntitiesWithNameNotInList(groupEntities));
        }
        return groupEntities;
    }

    private OpeningHoursGroupEntity getRandomizedGroupEntitiesWithNameNotInList(List<OpeningHoursGroupEntity> openingHoursGroupEntities) {
        List<String> usedNames = openingHoursGroupEntities.stream().map(OpeningHoursGroupEntity::getName).toList();
        ArrayList<String> possibleNames = new ArrayList<>(groupDescription);
        possibleNames.removeAll(usedNames);
        return new OpeningHoursGroupEntity()
                .setName(getRandomFromArray(possibleNames))
                .setRules(Collections.EMPTY_LIST);
    }

    private List<OpeningHoursRuleEntity> getNonEmptyListOfOpeningRules(int length) {
        List<OpeningHoursRuleEntity> OpeningHoursRules = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            OpeningHoursRules.add(getRandomizedOpeningHoursRuleEntityWithNameNotInList(OpeningHoursRules));
        }
        return OpeningHoursRules;
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

    private OpeningHoursRuleEntity getRandomizedOHRuleEntityWithNameNotInList(List<OpeningHoursRuleEntity>openingHoursRuleEntities) {
        List<String> usedNames = openingHoursRuleEntities.stream().map(OpeningHoursRuleEntity::getName).toList();
        ArrayList<String> possibleNames = new ArrayList<>(namesAndRules.keySet());
        possibleNames.removeAll(usedNames);
        String randomKey = getRandomFromKey(new ArrayList<>(possibleNames));
        return new OpeningHoursRuleEntity()
                .setName(randomKey)
                .setRule(namesAndRules.get(randomKey));
    }

    private  String getRandomFromKey(List<String> namesAndRulesKeys) {
        if (namesAndRulesKeys.isEmpty()) {
            //Hit skal man ikke komme
            return null;
        }
        Random random = new Random();
        return namesAndRulesKeys.get(random.nextInt(namesAndRulesKeys.size()));
    }

    private List<OpeningHoursGroupEntity> getRandomLengthListOfOpeningHoursGroupEntities() {
        Random random = new Random();
        int numberOfGroupEntities = 1 + random.nextInt(3);
        List<OpeningHoursGroupEntity> groupEntities = new ArrayList<>();
        for (int i = 0; i <= numberOfGroupEntities; i++) {
            groupEntities.add(getRandomizedGroupEntitiesWithNameNotInList(groupEntities));
        }
        return groupEntities;
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

    private ServiceType getRandomServiceType() {
        Random random = new Random();
        return ServiceType.values()[random.nextInt(ServiceType.values().length)];
    }

    private OpeningHoursGroupEntity getRandomizedOpeningHoursGroupEntity() {
        return new OpeningHoursGroupEntity()
                .setName(getRandomFromArray(groupDescription))
                .setRules(Collections.EMPTY_LIST);
    }

}
