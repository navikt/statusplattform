package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroupEntity;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class UpTimeCalculatorTest {
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

    private final OpeningHoursRepository openingHoursRepository = new OpeningHoursRepository(dbContext);

    private final UpTimeCalculator upTimeCalculator = new UpTimeCalculator(dbContext);

    private LocalDate nullDateEntry = null;

    private LocalDate dateGreaterThaCurrentDate = LocalDate.of(2025, 3, 1);

    private LocalDate normalFridayStartOfMonth = LocalDate.of(2024, 3, 1);
    private LocalDate thirdOfMarch = LocalDate.of(2024, 3, 3);
    private LocalDate normalFridayEndOfMonth = LocalDate.of(2024, 3, 30);
    private LocalDate weekendEndOfMonth = LocalDate.of(2024, 3, 31);

    private final ZonedDateTime todaysDate = ZonedDateTime.now();
    private final ZonedDateTime yesterdayDate = todaysDate.minusDays(1);

    private final ZonedDateTime daysBack = todaysDate.minusDays(2);

    private final ZonedDateTime twoDaysBack = todaysDate.minusDays(2);

    private final ZonedDateTime minusTwoMonths = yesterdayDate.minusMonths(2);

    private final ZonedDateTime minusOneYear = yesterdayDate.minusYears(1);


    private final ArrayList<String> rules = new ArrayList<>(Arrays.asList("24.12.???? ? 1-5 09:00-14:00", "06.04.2023 ? ? 08:00-17:00", "??.??.???? ? ? 07:00-21:00"));

    private final ArrayList<String> ruleNames = new ArrayList<>(Arrays.asList("ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    private final ArrayList<String> groupDescription = new ArrayList<>(Arrays.asList("Local maintenance", "Collaborative maintenance", "Early closing", "National Holidays"));

    @Test
    void getTotalOpeningHoursMinutes() {
        //Arrange

        //Add rule
        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity();
        rule.setName(ruleNames.getFirst());
        rule.setRule(rules.get(2));
        UUID ruleId = openingHoursRepository.save(rule);
        rule.setId(ruleId);
        //Add rule to group
        OpeningHoursGroupEntity group = new OpeningHoursGroupEntity().setName("Ny gruppe").setRules(List.of(ruleId));
        UUID groupId = openingHoursRepository.saveGroup(group);
        group.setId(groupId);
        //Add service
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        //add group to service
        openingHoursRepository.setOpeningHoursToService(groupId, serviceId);


        List<RecordEntity> records = generateRandomizedRecordEntities(service, 1);
        records.forEach(record -> {
            int min = 1;
            int max = 1;
            ZonedDateTime now = ZonedDateTime.now();
            int numberOfDays = ThreadLocalRandom.current().nextInt(min, max + 1);
            System.out.println("numberOfDays: " + numberOfDays);
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


        UpTimeTotal uptimeOpenAllTheTime = upTimeCalculator.calculateUpTimeForService(serviceId, daysBack, todaysDate);
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
