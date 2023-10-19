package nav.portal.core.repositories;

import nav.portal.core.entities.HelpTextEntity;
import nav.portal.core.entities.ServiceEntity;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

public class HelpTextRepositoryTest {
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

    private final HelpTextRepository helpTextRepository = new HelpTextRepository(dbContext);


    @Test
    void save() {
        //Arrange
        HelpTextEntity helpText = SampleData.getRandomizedHelpTextEntity();
        //Act
        long nr = helpTextRepository.save(helpText);
        helpText.setNr(nr);
        Optional<HelpTextEntity> retrievedHelpText = helpTextRepository.retrieve(nr);
        //Assert
        retrievedHelpText.ifPresent(helpTextEntity -> Assertions.assertThat(helpTextEntity).isEqualTo(helpText));
    }

}
