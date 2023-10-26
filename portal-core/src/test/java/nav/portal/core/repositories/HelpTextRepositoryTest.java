package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.HelpTextEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HelpTextRepositoryTest {

    private final DataSource dataSource = TestDataSource.create();

    private final DbContext dbContext = new DbContext();
    private final HelpTextRepository helpTextRepository = new HelpTextRepository(dbContext);
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

    @Test
    void save() {
        //Arrange
        HelpTextEntity helpText = SampleData.getRandomizedHelpTextEntity();
        //Act
        helpTextRepository.save(helpText);

        Optional<HelpTextEntity> retrievedHelpText = helpTextRepository.retrieve(helpText.getNr(), helpText.getType());
        //Assert
        retrievedHelpText.ifPresent(helpTextEntity -> Assertions.assertThat(helpTextEntity).isEqualTo(helpText));
    }

    @Test
    void update() {
        //Arrange
        HelpTextEntity helpText = SampleData.getRandomizedHelpTextEntity();
        //Act
        helpTextRepository.save(helpText);
        Optional<HelpTextEntity>helpTextBefore = helpTextRepository.retrieve(helpText.getNr(), helpText.getType());
        helpText.setContent("Any other information");
        helpTextRepository.update(helpText);
        Optional<HelpTextEntity>helpTextAfter = helpTextRepository.retrieve(helpText.getNr(), helpText.getType());
        //Assert
        helpTextAfter.ifPresent(helpTextEntityAfter -> Assertions.assertThat(helpTextEntityAfter.getContent())
                .isNotEqualTo(helpTextBefore.get().getContent()));
    }

    @Test
    void retrieve() {
        //Arrange
        HelpTextEntity helpText = SampleData.getRandomizedHelpTextEntity();
        //Act
        helpTextRepository.save(helpText);
        Optional<HelpTextEntity>retrievedHelpText = helpTextRepository.retrieve(helpText.getNr(), helpText.getType());
        //Assert
        retrievedHelpText.ifPresent(helpTextEntity -> Assertions.assertThat(helpTextEntity).isEqualTo(helpText));
    }

    @Test
    void retrieveAll() {
        //Arrange
        List<HelpTextEntity> helpTexts = SampleData.getHelpTextEntityWithRandomServiceTypes();
        helpTexts.forEach(helpTextRepository::save);
        //Act
        List<HelpTextEntity>allHelpTexts = helpTextRepository.retrieveAllHelpTextEntity();
        //Assert
        Assertions.assertThat(allHelpTexts.size()).isEqualTo(helpTexts.size());
        Assertions.assertThat(allHelpTexts).containsAll(helpTexts);
    }

    @Test
    void retrieveAllWithType() {
        //Arrange
        List<HelpTextEntity> helpTexts = SampleData.getHelpTextEntityWithRandomServiceTypes();
        helpTexts.forEach(helpTextRepository::save);
        //Act
        List<HelpTextEntity>typeHelpTexts = new ArrayList<>();
        String serviceType = SampleData.getRandomServiceType().name();
        helpTexts.forEach(helpText->{
            if(helpText.getType().equals(ServiceType.valueOf(serviceType))){
                typeHelpTexts.add(helpText);
            }
        });
        List<HelpTextEntity>retrievedHelpTexts
                = helpTextRepository.retrieveAllWithType(ServiceType.valueOf(serviceType));
        //Assert
        Assertions.assertThat(retrievedHelpTexts.size()).isEqualTo(typeHelpTexts.size());
        Assertions.assertThat(typeHelpTexts).containsAll(retrievedHelpTexts);
        Assertions.assertThat(helpTexts).containsAll(retrievedHelpTexts);
    }

    @Test
    void retrieveAllWithServiceType() {
        //Arrange
        List<HelpTextEntity> helpTexts = SampleData.getHelpTextEntityWithRandomServiceTypes();
        helpTexts.forEach(helpTextRepository::save);
        //Act
        List<HelpTextEntity>serviceHelpTexts = new ArrayList<>();
        helpTexts.forEach(helpText->{
            if(helpText.getType().equals(ServiceType.TJENESTE)){
                serviceHelpTexts.add(helpText);
            }
        });
        List<HelpTextEntity>retrievedHelpTexts = helpTextRepository.retrieveAllWithServiceType();
        //Assert
        Assertions.assertThat(retrievedHelpTexts.size()).isEqualTo(serviceHelpTexts.size());
        Assertions.assertThat(serviceHelpTexts).containsAll(retrievedHelpTexts);
        Assertions.assertThat(helpTexts).containsAll(retrievedHelpTexts);
    }

    @Test
    void retrieveAllWithComponentType() {
        //Arrange
        List<HelpTextEntity> helpTexts = SampleData.getHelpTextEntityWithRandomServiceTypes();
        helpTexts.forEach(helpTextRepository::save);
        //Act
        List<HelpTextEntity>componentHelpTexts = new ArrayList<>();
        helpTexts.forEach(helpText->{
            if(helpText.getType().equals(ServiceType.KOMPONENT)){
                componentHelpTexts.add(helpText);
            }
        });
        List<HelpTextEntity>retrievedHelpTexts = helpTextRepository.retrieveAllWithComponentType();
        //Assert
        Assertions.assertThat(retrievedHelpTexts.size()).isEqualTo(componentHelpTexts.size());
        Assertions.assertThat(componentHelpTexts).containsAll(retrievedHelpTexts);
        Assertions.assertThat(helpTexts).containsAll(retrievedHelpTexts);
    }

    @Test
    void delete() {
        //Arrange
        HelpTextEntity helpText = SampleData.getRandomizedHelpTextEntity();
        //Act
        helpTextRepository.save(helpText);
        helpTextRepository.delete(helpText.getNr(), helpText.getType());
        Optional<HelpTextEntity>retrievedHelpText = helpTextRepository.retrieve(helpText.getNr(), helpText.getType());
        //Assert
        Assertions.assertThat(retrievedHelpText.isPresent()).isFalse();
    }

}
