package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.*;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class ServicesUpTimeRenumeratorTest {

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

    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

    private final RecordRepository recordRepository = new RecordRepository(dbContext);

    private final ServicesUpTimeRenumerator servicesUpTimeRenumerator = new ServicesUpTimeRenumerator(dbContext);

    private LocalDate nullDateEntry = null;

    private LocalDate dateGreaterThaCurrentDate = LocalDate.of(2025, 3, 1);

    private LocalDate normalFridayStartOfMonth = LocalDate.of(2024, 3, 1);
    private LocalDate thirdOfMarch = LocalDate.of(2024, 3, 3);
    private LocalDate normalFridayEndOfMonth = LocalDate.of(2024, 3, 30);
    private LocalDate weekendEndOfMonth = LocalDate.of(2024, 3, 31);

    private LocalDate todaysDate = LocalDate.now();
    private LocalDate yesterdayDate = todaysDate.minusDays(1);

    private LocalDate daysBack = todaysDate.minusDays(5);

    private LocalDate twoDaysBack = todaysDate.minusDays(2);

    private String ohNull = null;
    private String ohAlwaysDown = "00:00-00:00";
    private String ohSevenToFive = "07:00-17:00";

    String validNotThursdayOrSunday = "??.??.???? ? 1,2,3,5-6 07:00-21:00";  //Gyldig
    String validMondayToFriday = "??.??.???? ? 1,2,3,4,5 07:00-21:00";  //Gyldig
    String validMondayToSaturday = "??.??.???? ? 1,2,3,4,5,6 07:00-21:00";  //Gyldig

    String validMondayToSunday = "??.??.???? ? 1,2,3,4,5,6,7 00:00-23:59";  //Gyldig

    String closedAllTheTime = "??.??.???? ? 1,2,3,4,5,6,7 00:00-00:00";  //Gyldig

    private LocalDate minusTwoMonths = yesterdayDate.minusMonths(2);

    private LocalDate minusOneYear = yesterdayDate.minusYears(1);


    @Test
    void getTotalOpeningHoursMinutes() {



        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 1);
        records.forEach(record -> {
            int min = 1;
            int max = 1;
            ZonedDateTime now = ZonedDateTime.now();
            /*int numberOfDays = ThreadLocalRandom.current().nextInt(min, max + 1);
            System.out.println("numberOfDays: " + numberOfDays);
            ZonedDateTime daysBack = now.minusHours(now.getHour()).minusDays(numberOfDays);*/
            ZonedDateTime daysBack = now.minusHours(6).minusDays(0);
            record.setCreated_at(daysBack);
            record.setServiceId(service.getId());
            record.setStatus(ServiceStatus.OK);
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
            System.out.println("createdAt: " + record.getCreated_at());
            System.out.println("daysBackDate " + daysBack.toLocalDate());
            System.out.println("daysBackTime " + daysBack.toLocalTime());
            System.out.println("todays date " + now.toLocalDate());
            System.out.println("todays time " + now.toLocalTime());
        });
        List<RecordEntity> retrievedRecordsBefore = recordRepository.getRecordsOlderThan(0);
        for (RecordEntity r : retrievedRecordsBefore) {
            System.out.println(r.getStatus());
        }


        UpTimeTotal uptimeOpenAllTheTime = servicesUpTimeRenumerator.calculateUpTimeForService(serviceId, daysBack, todaysDate, validMondayToSunday);
        System.out.println("Actual Up Time: " + uptimeOpenAllTheTime.getSumOfActualUptime());
        System.out.println("Expected Uptime: " + uptimeOpenAllTheTime.getSumOfExpectedUptime());





    }

    private List<RecordEntity> generateRandomizedRecordEntities(ServiceEntity serviceEntity, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The argument amount refers to size of list, must be a non-zero, positive integer.");
        }
        List<RecordEntity> records = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            records.add(SampleData.getRandomizedRecordEntityForService(serviceEntity));
        }
        return records;
    }

}
