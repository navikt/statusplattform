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
            oHRuleDto.setId(oHRuleDto.getId());
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
        });
        //Act
       List<OHRuleDto>retrievedOHRulesDto = openingHoursController.getRules();
       //Assert
       //Assertions.assertThat(retrievedOHRulesDto.size()).isEqualTo(savedOHRulesDto.size());
    }

    @Test
    void newRule(){
        //Arrange
        OHRuleDto oHRuleDto = SampleDataDto.getRandomizedOHRuleDto();
        oHRuleDto.setId(oHRuleDto.getId());
        //Act
        OHRuleDto savedOHRuleDto = openingHoursController.newRule(oHRuleDto);
        OHRuleDto retrievedOHRuleDto = openingHoursController.getRule(oHRuleDto.getId());
        //Assert
        Assertions.assertThat(retrievedOHRuleDto).isEqualTo(oHRuleDto);
        Assertions.assertThat(retrievedOHRuleDto.getId()).isEqualTo(oHRuleDto.getId());
    }




}
