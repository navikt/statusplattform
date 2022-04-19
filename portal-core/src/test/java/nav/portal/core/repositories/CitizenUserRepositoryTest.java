package nav.portal.core.repositories;

import nav.portal.core.entities.CitizenUserEntity;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

public class CitizenUserRepositoryTest {
    private final DataSource dataSource = TestDataSource.create();

    private final DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
//        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    private final CitizenUserRepository citizenUserRepository = new CitizenUserRepository(dbContext);

    @Test
    void save() {
        //Arrange
        CitizenUserEntity user = SampleData.getRandomizedCitizenEntity();
        //Act
        UUID uuid = citizenUserRepository.save(user);
        user.setId(uuid);
        Optional<CitizenUserEntity> retrievedCitizen = citizenUserRepository.retrieve(uuid);
        //Assert
        retrievedCitizen.ifPresent(citizenUserEntity -> Assertions.assertThat(citizenUserEntity).isEqualTo(user));
    }
}
