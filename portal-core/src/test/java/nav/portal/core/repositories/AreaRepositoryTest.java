package nav.portal.core.repositories;

import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class AreaRepositoryTest {

    private SampleData sampleData = new SampleData();

    private DataSource dataSource = TestDataSource.create();

    private DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    private final AreaRepository areaRepository = new AreaRepository(dbContext);

    @Test
    void save() {
    }

    @Test
    void uppdate() {
    }

    @Test
    void retrieveOne() {
    }

    @Test
    void deleteArea() {
    }

    @Test
    void addServiceToArea() {
    }

    @Test
    void removeServiceFromArea() {
    }


    @Test
    void retriveAllShallow() {
    }

    @Test
    void retrieveAll() {
    }
}