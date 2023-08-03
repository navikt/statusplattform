package nav.portal.core.repositories;

import nav.portal.core.entities.OpeningHoursGroup;
import nav.portal.core.entities.OpeningHoursGroupEntity;
import nav.portal.core.entities.OpeningHoursRuleEntity;
import nav.portal.core.entities.ServiceEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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


    @Test
    void save() {
        //Arrange
        OpeningHoursRuleEntity rule = SampleData.getRandomizedOpeningRule();
        //Act
        UUID id = openingHoursRepository.save(rule);
        //Assert
        Assertions.assertThat(id).isNotNull();

    }

    @Test
    void update() {
        //Arrange
        List<OpeningHoursRuleEntity> rules = SampleData.getNonEmptyListOfOpeningRules(2);
        rules.forEach(rule -> {
            rule.setId(openingHoursRepository.save(rule));
        });
        UUID ruleId = rules.get(0).getId();
        Optional<OpeningHoursRuleEntity> before = openingHoursRepository.retriveRule(ruleId);
        OpeningHoursRuleEntity retrievedBefore = before.get();
        //Act
        rules.get(0).setName(rules.get(1).getName());
        rules.get(0).setRule(rules.get(1).getRule());
        rules.get(0).setId(rules.get(1).getId());
        openingHoursRepository.update(rules.get(0));
        Optional<OpeningHoursRuleEntity> after = openingHoursRepository.retriveRule(rules.get(0).getId());
        OpeningHoursRuleEntity retrievedAfter = after.get();
        //Assert
        Assertions.assertThat(retrievedBefore.getName()).isNotEqualToIgnoringCase(rules.get(0).getName());
        Assertions.assertThat(retrievedAfter.getName()).isEqualTo(rules.get(1).getName());
    }

    @Test
    void deleteOpeninghours() {
        //Assign
        OpeningHoursRuleEntity ruleToBeDeleted = SampleData.getRandomizedOpeningRule();
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
        OpeningHoursRuleEntity rule = SampleData.getRandomizedOpeningRule();
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
        List<OpeningHoursGroupEntity>openingHoursGroupEntities = SampleData.getNonEmptyListOfOpeningHoursGroupEntities(2);
        List<OpeningHoursGroupEntity>groupEntities = new ArrayList<>();
        openingHoursGroupEntities.forEach(openingHoursGroupEntity -> {
            groupEntities.add(openingHoursGroupEntity.setId(openingHoursRepository.saveGroup(openingHoursGroupEntity)));
        });
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
        List<OpeningHoursRuleEntity> rules = SampleData.getNonEmptyListOfOpeningRules(2);
        rules.forEach(rule -> {
            rule.setId(openingHoursRepository.save(rule));
        });
        UUID ruleId = rules.get(0).getId();
        OpeningHoursRuleEntity ruleForRetrieving = rules.get(0);
        //Act
        Optional<OpeningHoursRuleEntity> retrievedRule = openingHoursRepository.retriveRule(ruleForRetrieving.getId());
        //Assert
        Assertions.assertThat(retrievedRule).contains(ruleForRetrieving);
    }

    @Test
    void getAllOpeningHoursRules(){
        //Arrange
        List<OpeningHoursRuleEntity> rules = SampleData.getRandomLengthListOfOHRuleEntity();
        List<UUID>rulesId = new ArrayList<>();
        rules.forEach(rule -> {
            rule.setId(openingHoursRepository.save(rule));
            rulesId.add(rule.getId());
        });
        List<OpeningHoursRuleEntity> retrievedRulesBefore = new ArrayList<>();
        rulesId.forEach(ruleId ->{
            Optional<OpeningHoursRuleEntity> before = openingHoursRepository.retriveRule(ruleId);
            retrievedRulesBefore.add(before.get());
        });
        //Act
        List<OpeningHoursRuleEntity> retrievedRulesAfter = openingHoursRepository.getAllOpeningHoursRules();
        //Assert
        Assertions.assertThat(retrievedRulesAfter).containsAll(retrievedRulesBefore);
        Assertions.assertThat(retrievedRulesAfter.size()).isEqualTo(retrievedRulesBefore.size());
    }

    @Test
    void getAllOpeningHoursGroups(){
        //Arrange
        List<OpeningHoursGroupEntity>groups= SampleData.getRandomLengthListOfOpeningHoursGroupEntities();
        List<UUID>groupsId = new ArrayList<>();
        groups.forEach(group -> {
            group.setId(openingHoursRepository.saveGroup(group));
            groupsId.add(group.getId());
        });
        List<OpeningHoursGroup> retrievedGroupsBefore = new ArrayList<>();
        groupsId.forEach(groupId ->{
            Optional<OpeningHoursGroup> before = openingHoursRepository.retrieveOneGroup(groupId);
            retrievedGroupsBefore.add(before.get());
        });
        //Act
        List<OpeningHoursGroup> retrievedGroupsAfter = openingHoursRepository.getAllGroups();
        //Assert
        Assertions.assertThat(retrievedGroupsAfter).containsAll(retrievedGroupsBefore);
        Assertions.assertThat(retrievedGroupsAfter.size()).isEqualTo(retrievedGroupsBefore.size());
    }

    @Test
    void deleteOpeninghourGroup() {
        //Arrange
        OpeningHoursRuleEntity rule = SampleData.getRandomizedOpeningRule();
        UUID rule_id = openingHoursRepository.save(rule);
        rule.setId(rule_id);
        OpeningHoursGroupEntity group = new OpeningHoursGroupEntity().setName("Ny gruppe").setRules(List.of(rule_id));
        UUID group_id = openingHoursRepository.saveGroup(group);
        group.setId(group_id);
        Optional<OpeningHoursGroup> retrievedGroupBefore = openingHoursRepository.retrieveOneGroup(group_id);
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
        OpeningHoursRuleEntity rule = SampleData.getRandomizedOpeningRule();
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
        OpeningHoursRuleEntity rule = SampleData.getRandomizedOpeningRule();
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

    @Test
    void retrieveOneGroupComplex() {

        List<OpeningHoursRuleEntity> rules = SampleData.getNonEmptyListOfOpeningRules(6);
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

        List<OpeningHoursRuleEntity> rules = SampleData.getNonEmptyListOfOpeningRules(6);
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
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        /*Create group*/
        OpeningHoursGroupEntity group = SampleData.getRandomizedOpeningHoursGroupEntity();
        group.setId(openingHoursRepository.saveGroup(group));
        UUID groupId = group.getId();
        //Act
        openingHoursRepository.setOpeningHoursToService(groupId, serviceId);
        //Assert
        Optional<OpeningHoursGroup>retrievedGroup = openingHoursRepository.getOHGroupForService(serviceId);
        Assertions.assertThat(retrievedGroup.get().getId()).isEqualTo(group.getId());
    }

    @Test
    void removeOpeningHoursFromService() {
        //Arrange
        /*Create service*/
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        /*Create group*/
        OpeningHoursGroupEntity group = SampleData.getRandomizedOpeningHoursGroupEntity();
        group.setId(openingHoursRepository.saveGroup(group));
        UUID groupId = group.getId();
        openingHoursRepository.setOpeningHoursToService(groupId, serviceId);
        Optional<OpeningHoursGroup>retrievedGroupBefore = openingHoursRepository.getOHGroupForService(serviceId);
        //Act
        openingHoursRepository.removeOpeningHoursFromService(serviceId);
        Optional<OpeningHoursGroup>retrievedGroupAfter = openingHoursRepository.getOHGroupForService(serviceId);
        //Assert
        Assertions.assertThat(retrievedGroupBefore.get().getId()).isEqualTo(group.getId());
        Assertions.assertThat(retrievedGroupAfter).isEmpty();
    }

}