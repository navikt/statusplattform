package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.OpeningHoursGroup;
import nav.portal.core.entities.OpeningHoursRule;
import nav.portal.core.entities.OpeningHoursRuleEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
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
        boolean isDeleted = openingHoursRepository.deleteOpeninghours(ruleToBeDeleted.getId());
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
        OpeningHoursGroup group = new OpeningHoursGroup().setName("Ny gruppe").setRules(List.of(rule));
        //Act
        UUID group_id = openingHoursRepository.saveGroup(group);
        group.setId(group_id);
        Optional<OpeningHoursGroup> retrievedGroup = openingHoursRepository.retrieveOneGroup(group_id);
        //Assert
        Assertions.assertThat(retrievedGroup).isPresent();
        Assertions.assertThat(retrievedGroup.get().getRules()).contains(rule);
        Assertions.assertThat(retrievedGroup.get()).isEqualTo(group);
    }

    /*@Test
    void updateGroup() {
        //Arrange
        String updatedOpeningHoursName = "Any other group";
        OpeningHoursRuleEntity rule = SampleData.getRandomizedOpeningRule();
        UUID rule_id = openingHoursRepository.save(rule);
        rule.setId(rule_id);
        OpeningHoursGroup group = new OpeningHoursGroup().setName("Ny gruppe").setRules(List.of(rule));
        UUID group_id = openingHoursRepository.saveGroup(group);
        group.setId(group_id);
        Optional<OpeningHoursGroup> retrievedGroupBefore = openingHoursRepository.retrieveOneGroup(group_id);
        group.setName(updatedOpeningHoursName);
        //Act
        openingHoursRepository.update(group);
        //Assert
    }*/

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
    void deleteOpeninghourGroup() {
        //Arrange
        OpeningHoursRuleEntity rule = SampleData.getRandomizedOpeningRule();
        UUID rule_id = openingHoursRepository.save(rule);
        rule.setId(rule_id);
        OpeningHoursGroup group = new OpeningHoursGroup().setName("Ny gruppe").setRules(List.of(rule));
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
        OpeningHoursGroup group = new OpeningHoursGroup().setName("Ny gruppe").setRules(List.of(rule));
        UUID group_id = openingHoursRepository.saveGroup(group);
        //Act
        Optional<OpeningHoursGroup> retrievedGroup = openingHoursRepository.retrieveOneGroup(group_id);
        //Assert
        Assertions.assertThat(retrievedGroup).isPresent();
        Assertions.assertThat(retrievedGroup.get().getRules()).containsExactly(rule);
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
        OpeningHoursGroup group3 = new OpeningHoursGroup().setName("Gruppe3").setRules(List.of(rule2,rule3));
        group3.setId(openingHoursRepository.saveGroup(group3));
        //g4
        OpeningHoursGroup group4 = new OpeningHoursGroup().setName("Gruppe4").setRules(List.of(rule4,rule5));
        group4.setId(openingHoursRepository.saveGroup(group4));
        //g2
        OpeningHoursGroup group2 = new OpeningHoursGroup().setName("Gruppe2").setRules(List.of(group3,group4));
        group2.setId(openingHoursRepository.saveGroup(group2));

        //g1
        OpeningHoursGroup group1 = new OpeningHoursGroup().setName("Gruppe1").setRules(List.of(rule1,group2,rule6));
        group1.setId(openingHoursRepository.saveGroup(group1));

        //Act
        Optional<OpeningHoursGroup> retrievedGroup = openingHoursRepository.retrieveOneGroup(group1.getId());
        //Assert
        Assertions.assertThat(retrievedGroup).isPresent();
//        Assertions.assertThat(retrievedGroup.get().getRules()).containsExactlyElementsOf(rulesOfGroup2);
    }

}