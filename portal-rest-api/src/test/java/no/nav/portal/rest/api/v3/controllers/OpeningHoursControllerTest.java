package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.SampleData;
import nav.portal.core.repositories.TestDataSource;
import nav.portal.core.repositories.TestUtil;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.*;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class OpeningHoursControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final OpeningHoursController openingHoursController = new OpeningHoursController(dbContext);
    private final ServiceController serviceController = new ServiceController(dbContext,true);

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

   @Test
    void getRules() {
        //Arrange
        List<OHRuleDto> oHRulesDto = SampleDataDto.getRulesDto();
        List<OHRuleDto>savedOHRulesDto = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
        });
        //Act
       List<OHRuleDto>retrievedOHRulesDto = openingHoursController.getRules();
       //Assert
       Assertions.assertThat(retrievedOHRulesDto.size()).isEqualTo(savedOHRulesDto.size());
    }

    @Test
    void newRule(){
        //Arrange
        OHRuleDto oHRuleDto = SampleDataDto.getRandomizedOHRuleDto();
        //Act
        OHRuleDto savedOHRuleDto = openingHoursController.newRule(oHRuleDto);
        oHRuleDto.setId(oHRuleDto.getId());
        OHRuleDto retrievedOHRuleDto = openingHoursController.getRule(oHRuleDto.getId());
        //Assert
        Assertions.assertThat(retrievedOHRuleDto).isEqualTo(oHRuleDto);
        Assertions.assertThat(retrievedOHRuleDto.getId()).isEqualTo(oHRuleDto.getId());
    }

    @Test
    void deleteRule() {
        //Arrange
        List<OHRuleDto>oHRulesDtos = SampleDataDto.getNonEmptyListOfOHRuleDto(2);
        List<OHRuleDto>oHRulesDtoBefore = new ArrayList<>();
        oHRulesDtos.forEach(oHRuleDto -> {
            oHRulesDtoBefore.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
        });
        OHRuleDto ruleToBeDeleted = oHRulesDtoBefore.get(0);
        //Act
        openingHoursController.deleteRule(oHRulesDtoBefore.get(0).getId());
        List<OHRuleDto>oHRulesDtoAfter = openingHoursController.getRules();
        //Assert
        Assertions.assertThat(oHRulesDtoBefore.size()).isEqualTo(2);
        Assertions.assertThat(oHRulesDtoAfter.size()).isEqualTo(1);
        Assertions.assertThat(oHRulesDtoBefore).contains(ruleToBeDeleted);
        Assertions.assertThat(oHRulesDtoAfter).doesNotContain(ruleToBeDeleted);

    }

    @Test
    void getRule() {
        List<OHRuleDto> oHRulesDto = SampleDataDto.getRulesDto();
        List<OHRuleDto>savedOHRulesDto = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
        });
        //Act
        OHRuleDto retrievedOHRuleDto = openingHoursController.getRule(savedOHRulesDto.get(0).getId());
        //Assert
        Assertions.assertThat(retrievedOHRuleDto).isEqualTo(savedOHRulesDto.get(0));
        Assertions.assertThat(retrievedOHRuleDto.getId()).isEqualTo(savedOHRulesDto.get(0).getId());
    }

    /*@Test
    void getGroups() {
        //Arrange
        List<OHGroupThinDto> oHGroupsThinDto= SampleDataDto.getGroupsThinDto();
        List<OHGroupThinDto>savedOHGroupsThinDto = new ArrayList<>();
        oHGroupsThinDto.forEach(oHGroupThinDto -> {
            savedOHGroupsThinDto.add(openingHoursController.newGroup(oHGroupThinDto));
            oHGroupThinDto.setId(oHGroupThinDto.getId());
        });
        //Act
        List<OHGroupThinDto>retrievedOHOHGroupsThinDto = openingHoursController.getGroups();
        //Assert
        Assertions.assertThat(retrievedOHOHGroupsThinDto.size()).isEqualTo(savedOHGroupsThinDto.size());
    }*/

    @Test
    void newGroup(){
        //Arrange
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        savedOHGroupThinDto.setId(savedOHGroupThinDto.getId());
        //Act
        OHGroupDto retrievedOHGroupThinDto = openingHoursController.getGroup(savedOHGroupThinDto.getId());
        //Assert

    }






}
