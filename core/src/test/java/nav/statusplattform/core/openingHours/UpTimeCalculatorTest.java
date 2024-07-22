package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroupEntity;
import nav.statusplattform.core.entities.OpeningHoursRuleEntity;
import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.*;
import org.assertj.core.internal.bytebuddy.implementation.bytecode.Addition;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


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

    private ZonedDateTime nullDateEntry = null;

    private final ZonedDateTime to = ZonedDateTime.now();

    private final ZonedDateTime toMinusTwoHours = to.minusHours(2);

    private final ZonedDateTime todaysDate = ZonedDateTime.now();
    private final ZonedDateTime yesterdayDate = todaysDate.minusDays(1);

    private final ZonedDateTime backOneDay = ZonedDateTime.now().minusDays(1);

    private final ZonedDateTime fiveDaysBack = todaysDate.minusDays(5);

    private final ZonedDateTime daysBackMinusTwohours = todaysDate.minusDays(0).minusHours(2);

    private final ZonedDateTime beforeOpeningHoursEnd = todaysDate.with(LocalTime.of(6, 30));

    private final ZonedDateTime beforeOpeningHoursStart = todaysDate.with(LocalTime.of(6, 30).minusHours(4));

    private final ZonedDateTime recordBeforeOpeningHours = todaysDate.with(LocalTime.of(6, 30).minusHours(2));

    private final ZonedDateTime daysForward = todaysDate.minusDays(0).plusHours(6);

    private final ZonedDateTime twoDaysBack = todaysDate.minusDays(2);

    private final ZonedDateTime minusTwoMonths = yesterdayDate.minusMonths(2);

    private final ZonedDateTime minusOneYear = yesterdayDate.minusYears(1);


    private final ArrayList<String> rules = new ArrayList<>(Arrays.asList("24.12.???? ? 1-5 09:00-14:00", "06.04.2023 ? ? 08:00-17:00", "??.??.???? ? ? 07:00-17:00"));

    private final ArrayList<String> ruleNames = new ArrayList<>(Arrays.asList("ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at"));

    private final ArrayList<String> groupDescription = new ArrayList<>(Arrays.asList("Local maintenance", "Collaborative maintenance", "Early closing", "National Holidays"));

    @Test
    void assertionThrownForADateOFNull() {
        //Arrange
        //Add service
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        //Assert
        //Throws an exception if the to and from period is of  yyyy-MM-dd - HH:mm:ss format
        Throwable exception = assertThrows(IllegalStateException.class, () ->
                upTimeCalculator.calculateUpTimeForService(serviceId, nullDateEntry, todaysDate));
        assertEquals("Arguments for DateEntry must consist of a date and time of 'yyyy-MM-dd - HH:mm:ss", exception.getMessage());
    }

    @Test
    void assertionThrownForDatEntryGreaterThanCurrentDate() {
        //Arrange
        //Add service
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        //Assert
        //Throws an exception if the date entries are greater than the previousRecordCurrentDay date

        //Greater than one day
        Throwable exceptionOneDay = assertThrows(IllegalStateException.class, () ->
                upTimeCalculator.calculateUpTimeForService(serviceId, yesterdayDate, todaysDate.plusDays(1)));
        assertEquals("Arguments for DateEntry cannot be greater than the previousRecordCurrentDay date and time", exceptionOneDay.getMessage());

        //Greater than one hour
        Throwable exceptionOneHour = assertThrows(IllegalStateException.class, () ->
                upTimeCalculator.calculateUpTimeForService(serviceId, yesterdayDate, todaysDate.plusHours(1)));
        assertEquals("Arguments for DateEntry cannot be greater than the previousRecordCurrentDay date and time", exceptionOneHour.getMessage());

        //Greater than two minutes
        Throwable exceptionTwoMinutes = assertThrows(IllegalStateException.class, () ->
                upTimeCalculator.calculateUpTimeForService(serviceId, yesterdayDate, todaysDate.plusMinutes(2)));
        assertEquals("Arguments for DateEntry cannot be greater than the previousRecordCurrentDay date and time", exceptionTwoMinutes.getMessage());

        //Greater than three seconds
        Throwable exceptionThreeSeconds = assertThrows(IllegalStateException.class, () ->
                upTimeCalculator.calculateUpTimeForService(serviceId, yesterdayDate, todaysDate.plusSeconds(3)));
        assertEquals("Arguments for DateEntry cannot be greater than the previousRecordCurrentDay date and time", exceptionThreeSeconds.getMessage());
    }

    @Test
    void recordEntitiesAssertions() {
        //Arrange
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

        //Assert
        //Throws an exception if no records are found
        Throwable exception = assertThrows(NullPointerException.class, () ->
                upTimeCalculator.calculateUpTimeForService(serviceId, yesterdayDate, todaysDate));
        assertEquals("Records not found for serviceId: " + serviceId, exception.getMessage());
    }


    @Test
    void getTotalOpeningHoursMinutesForDurationUnderADay() {
        //Arrange
        //Add rule
        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity();
        rule.setName(ruleNames.getFirst());
        rule.setRule(rules.get(2)); //"??.??.???? ? ? 07:00-17:00" open all days
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

        //Add record
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 1);
        records.forEach(record -> {
            int min = 1;
            int max = 1;
            ZonedDateTime now = ZonedDateTime.now();
            int numberOfDays = ThreadLocalRandom.current().nextInt(min, max + 1);
            record.setCreated_at(daysBackMinusTwohours);
            record.setServiceId(service.getId());
            record.setStatus(ServiceStatus.OK);
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });
        List<RecordEntity> retrievedRecordsBefore = recordRepository.getRecordsOlderThan(0);
        for (RecordEntity r : retrievedRecordsBefore) {
            System.out.println(r.getStatus());
            System.out.println(r.getCreated_at());
        }

        //calculate the number of minutes
        LocalTime openingTime = OpeningHoursParser.getOpeningTime(rules.get(2).substring(15));
        LocalTime closingTime = OpeningHoursParser.getClosingTime(rules.get(2).substring(15));


        long totalUpTimeMinutes1 = getDurationInMinutes(openingTime, closingTime, to);

        long totalUpTimeMinutes2 = getDurationInMinutes(openingTime, closingTime, to);

        //Act
        // within opening hours
        UpTimeTotal uptimeOpenAllTheTime1 = upTimeCalculator.calculateUpTimeForService(serviceId, toMinusTwoHours, to);

        UpTimeTotal uptimeOpenAllTheTime2 = upTimeCalculator.calculateUpTimeForService(serviceId, beforeOpeningHoursStart, to);

        //Assert
        //Record under a day within opening hours end time during working hours
        Assertions.assertEquals(uptimeOpenAllTheTime1.getSumOfExpectedUptime(), totalUpTimeMinutes1);

        //Assertions starts before working hours
        Assertions.assertEquals(uptimeOpenAllTheTime2.getSumOfExpectedUptime(), totalUpTimeMinutes2);
    }

    @Test
    void getTotalOpeningHoursMinutesForDurationOverSeveralDays() {
        //Arrange
        //Add rule
        OpeningHoursRuleEntity rule = new OpeningHoursRuleEntity();
        rule.setName(ruleNames.getFirst());
        rule.setRule(rules.get(2)); //"??.??.???? ? ? 07:00-17:00" open all days
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

        //Add record
        List<RecordEntity> records = generateRandomizedRecordEntities(service, 1);
        records.forEach(record -> {
            int min = 1;
            int max = 1;
            ZonedDateTime now = ZonedDateTime.now();
            int numberOfDays = ThreadLocalRandom.current().nextInt(min, max + 1);
            record.setCreated_at(fiveDaysBack);
            record.setServiceId(service.getId());
            record.setStatus(ServiceStatus.OK);
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });
        List<RecordEntity> retrievedRecordsBefore = recordRepository.getRecordsOlderThan(0);
        for (RecordEntity r : retrievedRecordsBefore) {
            System.out.println(r.getStatus());
            System.out.println(r.getCreated_at());
        }

        //calculate the number of minutes
        LocalTime openingTime = OpeningHoursParser.getOpeningTime(rules.get(2).substring(15));
        LocalTime closingTime = OpeningHoursParser.getClosingTime(rules.get(2).substring(15));


        long totalUpTimeMinutes1 = getDurationInMinutes(openingTime, closingTime, backOneDay);

        //Act
        // within opening hours
        UpTimeTotal uptimeOpenAllTheTime1 = upTimeCalculator.calculateUpTimeForService(serviceId, fiveDaysBack, backOneDay);

        //Assert
        //Record under a day within opening hours end time during working hours
        //Assertions.assertEquals(uptimeOpenAllTheTime1.getSumOfExpectedUptime(), totalUpTimeMinutes1);
        System.out.println("total uptime minutes " + uptimeOpenAllTheTime1.getSumOfExpectedUptime());
        System.out.println("actual uptime minutes " + uptimeOpenAllTheTime1.getSumOfActualUptime());


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

    private long getDurationInMinutes(LocalTime openingTime, LocalTime closingTime, ZonedDateTime durationEnd) {
        Long totalUpTimeMinutes = 0L;

        if (durationEnd.toLocalTime().isBefore(closingTime) && durationEnd.toLocalTime().isAfter(openingTime)) {
            ZonedDateTime startOfDay = durationEnd.withHour(openingTime.getHour()).withMinute(openingTime.getMinute());
            totalUpTimeMinutes = Duration.between(startOfDay, durationEnd).toMinutes();
        } else if (durationEnd.toLocalTime().isAfter(closingTime)) {
            //add the duration of the last date with its respective opening and ending hours
            totalUpTimeMinutes = Duration.between(durationEnd.withHour(openingTime.getHour()).withMinute(openingTime.getMinute()), durationEnd.withHour(closingTime.getHour()).withMinute(closingTime.getMinute())).toMinutes();
        }
        return totalUpTimeMinutes;

    }

}
