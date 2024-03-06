package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.repositories.TestDataSource;
import nav.statusplattform.core.repositories.TestUtil;
import no.nav.statusplattform.generated.api.HelpTextDto;
import org.actioncontroller.HttpRequestException;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static no.nav.statusplattform.generated.api.ServiceTypeDto.KOMPONENT;
import static no.nav.statusplattform.generated.api.ServiceTypeDto.TJENESTE;

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
    void saveTheSame() {
        //Arrange
        HelpTextDto helpTextDto = SampleDataDto.getRandomizedHelpTextDto();
        //Act
        HelpTextDto savedHelpTextDto = helpTextController.newHelpText(helpTextDto);
        //Act
        try {
            HelpTextDto _DuplicateSavedHelpTextDto = helpTextController.newHelpText(helpTextDto);
        } catch (HttpRequestException e) {
            Assertions.assertThat(e.getMessage().contains("finnes allerede"));
        }
        //Assert
        List<HelpTextDto>retrievedHelpTextDto = helpTextController.getAllHelpTexts();
        Assertions.assertThat(retrievedHelpTextDto.size()).isEqualTo(1);
        Assertions.assertThat(retrievedHelpTextDto.get(0)).isEqualTo(savedHelpTextDto);
        Assertions.assertThat(retrievedHelpTextDto.get(0)).isEqualTo(helpTextDto);
    }
    /*@Test
    void updateHelpText() {
        // TODO: This test is flaky... Must fix
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
    }*/

    @Test
    void getHelpText() {
        //Arrange
        HelpTextDto helpTextDto = SampleDataDto.getRandomizedHelpTextDto();
        HelpTextDto helpTextDtoBefore = helpTextController.newHelpText(helpTextDto);
        //Act
        HelpTextDto helpTextDtoAfter =
               helpTextController.getHelpText(helpTextDto.getNumber(),helpTextDto.getType());
        //Assert
        Assertions.assertThat(helpTextDtoAfter).isEqualTo(helpTextDtoBefore);

    }

    @Test
    void getHelpTextServices() {
        //Arrange
        List<HelpTextDto> helpTextDtos = SampleDataDto.getRandomNumberOfHelpTextDtos();
        helpTextDtos.forEach(helpTextController::newHelpText);
        int servicesTypeCount = 0;
        for(HelpTextDto h: helpTextDtos){
            if(h.getType().equals(TJENESTE)){
                servicesTypeCount++;
            }
        }
        //Act
        List<HelpTextDto> retrievedHelpTextServices = helpTextController.getHelpTextServices();
        //Assert
        Assertions.assertThat(retrievedHelpTextServices.size()).isEqualTo(servicesTypeCount);
    }

    @Test
    void getHelpTextComponents() {
        //Arrange
        List<HelpTextDto> helpTextDtos = SampleDataDto.getRandomNumberOfHelpTextDtos();
        helpTextDtos.forEach(helpTextController::newHelpText);
        int componentsTypeCount = 0;
        for(HelpTextDto h: helpTextDtos){
            if(h.getType().equals(KOMPONENT)){
                componentsTypeCount++;
            }
        }
        //Act
        List<HelpTextDto> retrievedHelpTextComponents = helpTextController.getHelpTextComponents();
        //Assert
        Assertions.assertThat(retrievedHelpTextComponents.size()).isEqualTo(componentsTypeCount);
    }

    @Test
    void deleteHelpText() {
        //Arrange
        List<HelpTextDto> helpTextDtos = SampleDataDto.getRandomNumberOfHelpTextDtos();
        helpTextDtos.forEach(helpTextController::newHelpText);
        HelpTextDto toBeDeleted = helpTextDtos.get(0);
        //Act
        helpTextController.deleteHelpText(helpTextDtos.get(0));
        List<HelpTextDto> retrievedHelpTextAfter = helpTextController.getAllHelpTexts();
        //Assert
        Assertions.assertThat(retrievedHelpTextAfter.size())
                .isNotEqualTo(helpTextDtos.size());
        Assertions.assertThat(retrievedHelpTextAfter.size() + 1).isEqualTo(helpTextDtos.size());
        Assertions.assertThat(helpTextDtos).contains(toBeDeleted);
        Assertions.assertThat(retrievedHelpTextAfter).doesNotContain(toBeDeleted);
    }

}
