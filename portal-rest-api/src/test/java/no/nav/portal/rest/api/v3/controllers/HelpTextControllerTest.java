package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.enums.ServiceType;
import nav.portal.core.repositories.TestDataSource;
import nav.portal.core.repositories.TestUtil;
import no.portal.web.generated.api.*;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static no.portal.web.generated.api.ServiceTypeDto.TJENESTE;

public class HelpTextControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final HelpTextController helpTextController = new HelpTextController(dbContext);

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
    void getAllHelpTexts() {
        //Arrange
        List<HelpTextDto> helpTextDtos = SampleDataDto.getRandomNumberOfHelpTextDtos();
        helpTextDtos.forEach(helpTextController::newHelpText);
        //Act
        List<HelpTextDto> helpTextBeforeDtos = new ArrayList<>(helpTextDtos);
        List<HelpTextDto>helpTextAfterDtos = helpTextController.getAllHelpTexts();
        //Assert
        Assertions.assertThat(helpTextAfterDtos).containsExactlyInAnyOrderElementsOf(helpTextBeforeDtos);
        Assertions.assertThat(helpTextAfterDtos.size()).isEqualTo(helpTextBeforeDtos.size());
    }

    @Test
    void newHelpText() {
        //Arrange
        HelpTextDto helpTextDto = SampleDataDto.getRandomizedHelpTextDto();
        //Act
        HelpTextDto savedHelpTextDto = helpTextController.newHelpText(helpTextDto);
        //Assert
        List<HelpTextDto>retrievedHelpTextDto = helpTextController.getAllHelpTexts();
        Assertions.assertThat(retrievedHelpTextDto.size()).isEqualTo(1);
        Assertions.assertThat(retrievedHelpTextDto.get(0)).isEqualTo(savedHelpTextDto);
        Assertions.assertThat(retrievedHelpTextDto.get(0)).isEqualTo(helpTextDto);
    }

    @Test
    void updateHelpText() {
        //Arrange
        List<HelpTextDto> helpTextDtos = SampleDataDto.getNonEmptyListOfHelpTextDtos(2);
        helpTextDtos.forEach(helpTextDto -> {
            helpTextDto.setType(TJENESTE);
            helpTextController.newHelpText(helpTextDto);
        });
        List<HelpTextDto> retrievedHelpTextBefore = helpTextController.getAllHelpTexts();
        helpTextDtos.get(0).setContent(helpTextDtos.get(1).getContent());
        //Act
        helpTextController.updateHelpText(helpTextDtos.get(0));
        List<HelpTextDto> retrievedHelpTextAfter = helpTextController.getAllHelpTexts();
        //Assert
        Assertions.assertThat(retrievedHelpTextBefore.get(0).getContent())
                .isNotEqualTo(retrievedHelpTextBefore.get(1).getContent());
        Assertions.assertThat(retrievedHelpTextAfter.get(0).getContent())
                .isNotEqualTo(retrievedHelpTextBefore.get(0).getContent());

    }

}
