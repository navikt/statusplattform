package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.repositories.SampleData;
import nav.statusplattform.core.repositories.ServiceRepository;
import nav.statusplattform.core.repositories.TestDataSource;
import nav.statusplattform.core.repositories.TestUtil;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static nav.statusplattform.core.openingHours.ServicesUpTimeRenumerator.recordRepository;


public class ServicesUpTimeRenumeratorTest {

    private final DataSource dataSource = TestDataSource.create();

    private final DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

    private LocalDate nullDateEntry = null;

    private LocalDate dateGreaterThaCurrentDate = LocalDate.of(2025, 3, 1);

    private LocalDate normalFridayStartOfMonth = LocalDate.of(2024, 3, 1);
    private LocalDate thirdOfMarch = LocalDate.of(2024, 3, 3);
    private LocalDate normalFridayEndOfMonth = LocalDate.of(2024, 3, 30);
    private LocalDate weekendEndOfMonth = LocalDate.of(2024, 3, 31);

    private LocalDate todaysDate = LocalDate.now();
    private LocalDate yesterdayDate = todaysDate.minusDays(1);

    private LocalDate fiveDaysBack = todaysDate.minusDays(5);

    private LocalDate twoDaysBack = todaysDate.minusDays(2);

    private String ohNull = null;
    private String ohAlwaysDown = "00:00-00:00";
    private String ohSevenToFive = "07:00-17:00";

    String validNotThursdayOrSunday = "??.??.???? ? 1,2,3,5-6 07:00-21:00";  //Gyldig
    String validMondayToFriday = "??.??.???? ? 1,2,3,4,5 07:00-21:00";  //Gyldig
    String validMondayToSaturday = "??.??.???? ? 1,2,3,4,5,6 07:00-21:00";  //Gyldig

    String validMondayToSunday = "??.??.???? ? 1,2,3,4,5,6,7 00:00-23:59";  //Gyldig

    private LocalDate minusTwoMonths = yesterdayDate.minusMonths(2);

    private LocalDate minusOneYear = yesterdayDate.minusYears(1);


    @Test
    void getTotalOpeningHoursMinutes() {

        //        int incorrectDateEntry = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(nullDateEntry, normalFridayEndOfMonth, ohSevenToFive);//incorrectDateEntry
//        int entryDateGreaterThankCurrentDate = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, dateGreaterThaCurrentDate, ohSevenToFive);//dateGreaterThanCurrentDate
//        int ohEqualsNull = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, normalFridayEndOfMonth, ohNull);//opening hours is equal to null
//        int serviceAlwaysDown = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, normalFridayEndOfMonth, ohAlwaysDown);//service always down

          /*int currentDateMinutes = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(todaysDate, todaysDate, ohSevenToFive);
          System.out.println("currentDateMinutes: " + currentDateMinutes);*/

        /*int yesterdaytoCurrentDay = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(yesterdayDate, todaysDate, ohSevenToFive);
        System.out.println("yesterdaytoCurrentDay: " + yesterdaytoCurrentDay);*/

        /*int fiveDaysBacktoCurrentDay = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(fiveDaysBack, todaysDate, ohSevenToFive);
        System.out.println("fiveDaysBacktoCurrentDay: " + fiveDaysBacktoCurrentDay);*/

        /*int fiveDaysBacktoCurrentDay = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(fiveDaysBack, todaysDate, ohSevenToFive);
        System.out.println("fiveDaysBacktoCurrentDay: " + fiveDaysBacktoCurrentDay);*/

        /*int twoMonthsBack = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(minusTwoMonths, yesterdayDate, ohSevenToFive);
        System.out.println("twoMonthsBack: " + twoMonthsBack);*/

        /*int oneYearBack = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(minusOneYear, yesterdayDate, ohSevenToFive);
        System.out.println("oneYearBack: " + oneYearBack);*/

        /*int fiveDaysBacktoYesterDay = (int) ServicesUpTimeRenumerator.calculateUpTimeForService(fiveDaysBack, yesterdayDate, validNotThursdayOrSunday);
        System.out.println("fiveDaysBacktoYesterDay: " + fiveDaysBacktoYesterDay);*/

        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 12);
        records.forEach(record -> {
            int min = 2;
            int max = 5;
            ZonedDateTime now = ZonedDateTime.now();
            int numberOfDays = ThreadLocalRandom.current().nextInt(min, max + 1);
            ZonedDateTime fiveDaysBack = now.minusHours(now.getHour()).minusDays(numberOfDays);
            record.setCreated_at(fiveDaysBack);
            record.setServiceId(service.getId());
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });


        /*int validWorkDaysMondayToFriday = (int) ServicesUpTimeRenumerator.calculateUpTimeForService(serviceId, fiveDaysBack, yesterdayDate, validMondayToFriday);
        System.out.println("validWorkDaysMondayToFriday: " + validWorkDaysMondayToFriday);
        int validWeekDaysMondayToSaturday = (int) ServicesUpTimeRenumerator.calculateUpTimeForService(serviceId, fiveDaysBack, yesterdayDate, validMondayToSaturday);
        System.out.println("validWeekDaysMondayToSaturday: " + validWeekDaysMondayToSaturday);*/
        int validTimeMondayToSunday = (int) ServicesUpTimeRenumerator.calculateUpTimeForService(serviceId, fiveDaysBack, todaysDate, validMondayToSunday);
        System.out.println("validTimeMondayToSunday: " + validTimeMondayToSunday);

        /*int MarchTotal0130 = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, normalFridayEndOfMonth, ohSevenToFive);
        System.out.println("MarchTotal0130: " + MarchTotal0130);
        int MarchTotal0131 = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, weekendEndOfMonth, ohSevenToFive);
        System.out.println("MarchTotal0131: " + MarchTotal0131);
        int week9StartOfMarch = (int) ServicesUpTimeRenumerator.getTotalOpeningHoursMinutes(normalFridayStartOfMonth, thirdOfMarch, ohSevenToFive);
        System.out.println("week9StartOfMarch: " + week9StartOfMarch);*/

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
