package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.HelpTextEntity;
import nav.statusplattform.core.enums.ServiceType;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HelpTextRepositoryTest {

    private final DataSource dataSource = TestDataSource.create();

    private final DbContext dbContext = new DbContext();
    private final HelpTextRepository helpTextRepository = new HelpTextRepository(dbContext);
    private DbContextConnection connection;

    private final int MAX_NUM_OF_HELP_TEXT = 5;
    private ArrayList<String> helpTextDescriptions = new ArrayList<>(Arrays.asList(
            "Navnet på komponenten slik den omtales ut mot brukerne av komponenten",
            "Navnet på tjenesten slik den omtales ut mot brukerne av tjenesten",
            "Navnet på team slik det er skrevet i Teamkatalogen",
            "Link til et eventuelt dashboard eller monitor med mer detaljert informasjon. Eksempelvis Grafana dashboard",
            "URL til statusendepunkt som Statusplattformen skal polle for status",
            "Her kan man legge inn andre komponenter det er avhengigheter til. Informasjon om status på disse vil da vises i komponentbildet. Velg i liste og klikk Legg til for hver komponent.",
            "Her legger man inn tjenester hvor komponeten skal vises. Velg i liste og klikk Legg til for hver tjeneste."));

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
    void save() {
        //Arrange
        HelpTextEntity helpText = getRandomizedHelpTextEntity();
        //Act
        helpTextRepository.save(helpText);
        HelpTextEntity retrievedHelpText = helpTextRepository.retrieve(helpText.getNumber(), helpText.getType());
        //Assert
        Assertions.assertThat(retrievedHelpText).isEqualTo(helpText);
    }

    @Test
    void update() {
        //Arrange
        HelpTextEntity helpText = getRandomizedHelpTextEntity();
        //Act
        helpTextRepository.save(helpText);
        HelpTextEntity helpTextBefore = helpTextRepository.retrieve(helpText.getNumber(), helpText.getType());
        helpText.setContent("Any other information");
        helpTextRepository.update(helpText);
        HelpTextEntity helpTextAfter = helpTextRepository.retrieve(helpText.getNumber(), helpText.getType());
        //Assert
        Assertions.assertThat(helpTextAfter.getContent().equals(helpTextBefore.getContent())).isFalse();
    }

    @Test
    void retrieve() {
        //Arrange
        HelpTextEntity helpText = getRandomizedHelpTextEntity();
        //Act
        helpTextRepository.save(helpText);
        HelpTextEntity retrievedHelpText = helpTextRepository.retrieve(helpText.getNumber(), helpText.getType());
        //Assert
        Assertions.assertThat((retrievedHelpText).equals(helpText)).isTrue();
    }

    @Test
    void retrieveAll() {
        //Arrange
        List<HelpTextEntity> helpTexts =
                getHelpTextEntityWithRandomServiceTypes();
        helpTexts.forEach(helpTextRepository::save);
        //Act
        List<HelpTextEntity>allHelpTexts = helpTextRepository.retrieveAllHelpTexts();
        //Assert
        Assertions.assertThat(allHelpTexts.size()).isEqualTo(helpTexts.size());
        Assertions.assertThat(allHelpTexts).containsAll(helpTexts);
    }

    @Test
    void retrieveAllServices() {
        //Arrange
        List<HelpTextEntity> helpTexts = getHelpTextEntityWithRandomServiceTypes();
        helpTexts.forEach(helpTextRepository::save);
        //Act
        List<HelpTextEntity>serviceHelpTexts = new ArrayList<>();
        helpTexts.forEach(helpText->{
            if(helpText.getType().equals(ServiceType.TJENESTE)){
                serviceHelpTexts.add(helpText);
            }
        });
        List<HelpTextEntity>retrievedHelpTexts = helpTextRepository.retrieveHelpTextServices();
        //Assert
        Assertions.assertThat(retrievedHelpTexts.size()).isEqualTo(serviceHelpTexts.size());
        Assertions.assertThat(serviceHelpTexts).containsAll(retrievedHelpTexts);
        Assertions.assertThat(helpTexts).containsAll(retrievedHelpTexts);
    }

    @Test
    void retrieveAllComponents() {
        //Arrange
        List<HelpTextEntity> helpTexts = getHelpTextEntityWithRandomServiceTypes();
        helpTexts.forEach(helpTextRepository::save);
        //Act
        List<HelpTextEntity>componentHelpTexts = new ArrayList<>();
        helpTexts.forEach(helpText->{
            if(helpText.getType().equals(ServiceType.KOMPONENT)){
                componentHelpTexts.add(helpText);
            }
        });
        List<HelpTextEntity>retrievedHelpTexts = helpTextRepository.retrieveHelpTextComponents();
        //Assert
        Assertions.assertThat(retrievedHelpTexts.size()).isEqualTo(componentHelpTexts.size());
        Assertions.assertThat(componentHelpTexts).containsAll(retrievedHelpTexts);
        Assertions.assertThat(helpTexts).containsAll(retrievedHelpTexts);
    }

    @Test
    void delete() {
        //Arrange
        HelpTextEntity helpText = getRandomizedHelpTextEntity();
        helpTextRepository.save(helpText);
        //Act
        int isDeleted = helpTextRepository.delete(helpText);
        //Assert
        Assertions.assertThat(isDeleted).isEqualTo(1);
    }

    private HelpTextEntity getRandomizedHelpTextEntity() {
        int numberOfHelpTexts = SampleData.randomPositiveInteger(MAX_NUM_OF_HELP_TEXT);
        return new HelpTextEntity()
                .setNumber(numberOfHelpTexts)//up to five service and component help texts
                .setType(SampleData.getRandomServiceType())
                .setContent(SampleData.getRandomFromArray(helpTextDescriptions));
    }

    private List<HelpTextEntity> getHelpTextEntityWithRandomServiceTypes() {
        int numberOfServices = SampleData.randomPositiveInteger(MAX_NUM_OF_HELP_TEXT);//up to five service and component help texts
        int numberOfComponents = SampleData.randomPositiveInteger(MAX_NUM_OF_HELP_TEXT);//up to five service and component help texts
        List<HelpTextEntity> result = new ArrayList<>();
        for (int i = 1; i <= numberOfServices; i++) {
            result.add(getHelpTextEnity(ServiceType.TJENESTE, i));
        }

        for (int i = 1; i <= numberOfComponents; i++) {
            result.add(getHelpTextEnity(ServiceType.KOMPONENT, i));
        }
        return result;
    }

    private HelpTextEntity getHelpTextEnity(ServiceType serviceType, int number) {
        return new HelpTextEntity()
                .setNumber(number)
                .setType(serviceType)
                .setContent(SampleData.getRandomFromArray(helpTextDescriptions));
    }

}
