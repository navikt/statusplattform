package nav.portal.jobs.recordAggregation;


import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class RecordCompressorTest {

    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;



    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
    }

    @AfterEach
    void endConnection() {
        TestUtil.clearAllTableData(dbContext);
        connection.close();
    }

    @Test
    void basicSetUpTest() {
        List<ServiceEntity> myServices = SampleData.getNonEmptyListOfServiceEntityWithUid(10);
        ServiceEntity myService = myServices.get(0);
        int numberOfDays = 10;
        int minutesBetweenStatusUpdates = 100;
        //Under får vi generert satuser for alle tjenester hvert 100ede minutt 10 dager tilbake i tid:
        Map<UUID,Map<Integer,List<RecordEntity>>> resultForALl =
                MockDataGenerator.generateRandomStatusesForAllServices(myServices,numberOfDays,minutesBetweenStatusUpdates);

        //Under får vi generert satuser for en tjenester hvert 100ede minutt 10 dager tilbake i tid:
        Map<Integer,List<RecordEntity>> resultForOne =
                MockDataGenerator.generateRandomStatusesForOneServiceXNumberOfDaysBackInTime(myService, numberOfDays, minutesBetweenStatusUpdates);
        int i = 0;
    }
}