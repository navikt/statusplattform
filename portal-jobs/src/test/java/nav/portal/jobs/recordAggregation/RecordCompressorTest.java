package nav.portal.jobs.recordAggregation;


import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import nav.portal.core.util.MockDataGenerator;
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
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);
    private final  RecordCompressor recordCompressor = new RecordCompressor(dbContext);
    private final RecordRepository recordRepository = new RecordRepository(dbContext);

    private DbContextConnection connection;

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        recordCompressor.setDataSource(dataSource);
    }

    @AfterEach
    void endConnection() {
        TestUtil.clearAllTableData(dbContext);
        connection.close();
    }

    @Test
    void basic_SetUpTest_forAll() {
        //ARRRANGE
        List<ServiceEntity> myServices = SampleData.getNonEmptyListOfServiceEntity(10);
        myServices.forEach(s -> s.setId(serviceRepository.save(s)));
        int numberOfDays = 10;
        int minutesBetweenStatusUpdates = 100;
        //Under får vi generert statuser for alle tjenester hvert 100ede minutt 10 dager tilbake i tid:
        Map<UUID,Map<Integer,List<RecordEntity>>> resultForALl =
                MockDataGenerator.generateRandomStatusesForAllServices(myServices,numberOfDays,minutesBetweenStatusUpdates);

        //Vi bruker egen save metode i mockdatagenerater som setter created at explisit.
        //Dersom vi hadde brukt repository her ville alle statuser kommet inn med created_at nå.
        MockDataGenerator.saveRecordsToTableForAllServices(resultForALl,dbContext);
        //TODO METODEN UNDER FEILER AV UKJENT ÅRSAK,

        //TODO det er denne vi skal teste.




        //ACT
        recordCompressor.run();


        //ASSERT


    }
    @Test
    void test_one_service(){
        //ARRANGE
        ServiceEntity myService = SampleData.getRandomizedServiceEntity();
        myService.setId(serviceRepository.save(myService));

        int numberOfDays = 10;
        int minutesBetweenStatusUpdates = 60;
        //Under får vi generert satuser for en tjenester hvert 100ede minutt 10 dager tilbake i tid:
        Map<Integer,List<RecordEntity>> resultForOne =
                MockDataGenerator.generateRandomStatusesForOneServiceXNumberOfDaysBackInTime(myService, numberOfDays, minutesBetweenStatusUpdates);

        //Vi bruker egen save metode i mockdatagenerater som setter created at explisit.
        //Dersom vi hadde brukt repository her ville alle statuser kommet inn med created_at nå.
        MockDataGenerator.saveRecordsToTableForOneService(resultForOne,dbContext);

        //ACT
        recordRepository.getLatestRecord(myService.getId());
        recordCompressor.run();


        //ASSERT


    }
}