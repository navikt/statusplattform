package nav.portal.jobs.recordAggregation;


import nav.portal.core.entities.DailyStatusAggregationForServiceEntity;
import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import nav.portal.core.util.MockDataGenerator;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

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
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        connection.close();
    }

    @Test
    void basic_SetUpTest_forAll() {
        //ARRANGE
        //Denne testen generer records for alle tjenestene tilbake i tid, og ser at history objekter opprettes for alle etter komprimering.
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
        //ACT
        recordCompressor.run();
        //ASSERT
        Map<UUID,List<DailyStatusAggregationForServiceEntity>> compressedRecordsMap = new HashMap<>();
        myServices
                .forEach(s ->
                        compressedRecordsMap
                                //NB! dag 1 rekorder ikke er en del av kompression
                                .put(s.getId(), recordRepository.getServiceHistoryForNumberOfDays(numberOfDays,s.getId())));


        compressedRecordsMap.values().forEach(historyEntry -> {
                    Assertions.assertThat(historyEntry).isNotEmpty();

                    Assertions.assertThat(historyEntry.size()).isEqualTo(10); //NB! dag 1 er eskludert fra kompression
                }
        );

        //Komprimeringen skal resultere i sletting av alle records eldre enn i dag:
        Map<UUID, Map<LocalDate,List<RecordEntity>>>  recordsAfterCompression = recordRepository.getAllRecordsOrderedByServiceIdAndDate();
        Assertions.assertThat(recordsAfterCompression).isEmpty();


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
        List<DailyStatusAggregationForServiceEntity>compressedRecords =
                recordRepository.getServiceHistoryForNumberOfDays(numberOfDays, myService.getId());
        List<RecordEntity> outdatedRecords = recordRepository.getRecordsOlderThan(7);
        Assertions.assertThat(compressedRecords).isNotEmpty();
        Assertions.assertThat(compressedRecords.size()).isEqualTo(numberOfDays);//NB! dag 1 er ikke en del av kompression
        Assertions.assertThat(outdatedRecords).isEmpty();
    }


    @Test
    void shouldNotCompressWhenAlreadyCompressed() {
        //2x compressions on the same dataset should not result 2 DailyStatusAggregationForServiceEntity for a day
        //ARRANGE
        ServiceEntity myService = SampleData.getRandomizedServiceEntity();
        myService.setId(serviceRepository.save(myService));

        int numberOfDays = 3;
        int minutesBetweenStatusUpdates = 60;
        //Under får vi generert satuser for en tjenester hvert 100ede minutt 10 dager tilbake i tid:
        Map<Integer,List<RecordEntity>> resultForOne =
                MockDataGenerator.generateRandomStatusesForOneServiceXNumberOfDaysBackInTime(myService, numberOfDays, minutesBetweenStatusUpdates);

        //Vi bruker egen save metode i mockdatagenerater som setter created at explisit.
        //Dersom vi hadde brukt repository her ville alle statuser kommet inn med created_at nå.
        MockDataGenerator.saveRecordsToTableForOneService(resultForOne,dbContext);


        List<DailyStatusAggregationForServiceEntity> compressedRecords;

        List<DailyStatusAggregationForServiceEntity> doubleCompressedRecords;

        //ACT
        recordRepository.getLatestRecord(myService.getId());
        recordCompressor.run();
        compressedRecords = recordRepository.getServiceHistoryForNumberOfDays(numberOfDays, myService.getId());
        recordCompressor.run();
        doubleCompressedRecords = recordRepository.getServiceHistoryForNumberOfDays(numberOfDays, myService.getId());
        Assertions.assertThat(compressedRecords.size()).isEqualTo(doubleCompressedRecords.size());
    }

    @Test
    void shouldNotCompressTodaysRecords() {
        //Records that are created today should not be compressed
        //ARRANGE
        ServiceEntity myService = SampleData.getRandomizedServiceEntity();
        myService.setId(serviceRepository.save(myService));

        List<RecordEntity> generatedRecords = MockDataGenerator.generateRandomStatusesForServiceForOneDayXNumberOfDaysBackInTime(myService,0, 10);
        MockDataGenerator.saveRecordsToTableForOneServiceOneDay(generatedRecords,dbContext);

        //ACT
        recordCompressor.run();
        Optional<DailyStatusAggregationForServiceEntity> shouldBeEmpty = recordRepository.getServiceHistoryForServiceByDate(myService.getId(),LocalDate.now());

        Assertions.assertThat(shouldBeEmpty).isEmpty();
    }

    @Test
    void shouldCompressYesterdaysRecords() {
        //Records that are created today should not be compressed
        //ARRANGE
        ServiceEntity myService = SampleData.getRandomizedServiceEntity();
        myService.setId(serviceRepository.save(myService));

        List<RecordEntity> generatedRecords = MockDataGenerator.generateRandomStatusesForServiceForOneDayXNumberOfDaysBackInTime(myService,1, 10);
        MockDataGenerator.saveRecordsToTableForOneServiceOneDay(generatedRecords,dbContext);

        //ACT
        recordCompressor.run();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Optional<DailyStatusAggregationForServiceEntity> shouldBePresent = recordRepository.getServiceHistoryForServiceByDate(myService.getId(),yesterday);

        Assertions.assertThat(shouldBePresent).isNotEmpty();

    }
}