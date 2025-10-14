package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.SampleData;
import nav.statusplattform.core.repositories.TestDataSource;
import nav.statusplattform.core.repositories.TestUtil;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.generated.api.OHGroupThinDto;
import no.nav.statusplattform.generated.api.OHRuleDto;
import no.nav.statusplattform.generated.api.ServiceDto;
import no.nav.statusplattform.generated.api.UpTimeTotalsDto;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpTimeControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    private final UpTimeController upTimeController = new UpTimeController(dbContext);

    private final OpeningHoursController openingHoursController = new OpeningHoursController(dbContext);

    private final ServiceController serviceController = new ServiceController(dbContext);

    @BeforeEach
    void startConnection() {
        // Set timezone consistently across JVM, database, and test framework
        String timezone = "Europe/Oslo";
        System.setProperty("user.timezone", timezone);
        TimeZone.setDefault(TimeZone.getTimeZone(timezone));

        // Also set database timezone if using PostgreSQL
        System.setProperty("jdbc.timezone", timezone);

        connection = dbContext.startConnection(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
        // Reset all timezone settings
        System.clearProperty("user.timezone");
        System.clearProperty("jdbc.timezone");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        connection.close();
    }

    @Test
    void getServiceUpTimeSums1() {
        //Arrange
        //Rules set up
        List<OHRuleDto> oHRulesDto = SampleDataDto.getBasicRule();
        List<OHRuleDto> savedOHRulesDto = new ArrayList<>();
        List<UUID> savedOHRulesDtoIds = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
            savedOHRulesDtoIds.add(oHRuleDto.getId());
        });

        //Group set up
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //add rules to group
        savedOHGroupThinDto.setRules(savedOHRulesDtoIds);
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);

        //Create service Dto
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        serviceDto.setId(serviceDto.getId());
        service.setId(serviceDto.getId());

        //Create record
        RecordEntity record = SampleData.getRandomizedRecordEntity();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Oslo"));
        ZonedDateTime sevenDaysBack = now.minusHours(now.getHour()).minusDays(7);
        record.setCreated_at(sevenDaysBack);
        record.setServiceId(serviceDto.getId());
        record.setStatus(ServiceStatus.OK);
        record.setId(TestUtil.saveRecordBackInTime(record, dbContext));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());
        //Add record to service
        record.setServiceId(serviceDto.getId());

        //Act

        UpTimeTotalsDto retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                LocalDateTime.now().minusDays(1).toString(),
                LocalDateTime.now().toString());

        //Assert
        System.out.println(retrievedUpTimeTotalsDto.toString());
    }

    @Test
    void getServiceUpTimeSums2() {
        //Arrange
        //Rules set up
        List<OHRuleDto> oHRulesDto = SampleDataDto.getBasicRule();
        List<OHRuleDto> savedOHRulesDto = new ArrayList<>();
        List<UUID> savedOHRulesDtoIds = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
            savedOHRulesDtoIds.add(oHRuleDto.getId());
        });

        //Group set up
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //add rules to group
        savedOHGroupThinDto.setRules(savedOHRulesDtoIds);
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);

        //Create service Dto
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        serviceDto.setId(serviceDto.getId());
        service.setId(serviceDto.getId());

        //Create record
        RecordEntity record = SampleData.getRandomizedRecordEntity();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Oslo"));
        ZonedDateTime sevenDaysBack = now.minusHours(now.getHour()).minusDays(7);
        record.setCreated_at(sevenDaysBack);
        record.setServiceId(serviceDto.getId());
        record.setStatus(ServiceStatus.OK);
        record.setId(TestUtil.saveRecordBackInTimeForServiceStatusDeltatable(record, dbContext));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());
        //Add record to service
        record.setServiceId(serviceDto.getId());

        //Act

        UpTimeTotalsDto retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                LocalDateTime.now().minusDays(7).toString(),
                LocalDateTime.now().toString());

        //Assert
        System.out.println(retrievedUpTimeTotalsDto.toString());
    }

    @Test
    void getServiceUpTimeSums3() {
        //Arrange
        //Rules set up
        List<OHRuleDto> oHRulesDto = SampleDataDto.getBasicRule();
        List<OHRuleDto> savedOHRulesDto = new ArrayList<>();
        List<UUID> savedOHRulesDtoIds = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
            savedOHRulesDtoIds.add(oHRuleDto.getId());
        });

        //Group set up
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //add rules to group
        savedOHGroupThinDto.setRules(savedOHRulesDtoIds);
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);

        //Create service Dto
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        serviceDto.setId(serviceDto.getId());
        service.setId(serviceDto.getId());


        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        List<RecordEntity> records
                = SampleData.generateRecordsEntitiesForTimeSpan
                (service, LocalDateTime.now().minusMonths(1), LocalDateTime.now());

        records.forEach(record -> {
            record.setServiceId(serviceDto.getId());
            record.setId(TestUtil.saveRecordBackInTimeForServiceStatusDeltatable(record, dbContext));
        });

        //Act
        UpTimeTotalsDto retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                LocalDateTime.now().minusDays(30).toString(),
                LocalDateTime.now().toString());


        //Assert
        System.out.println(retrievedUpTimeTotalsDto.toString());
    }

    //Create records
    //march to april, 2025 from csv file.



    @Test
        //Normal work day rule: ??.??.???? ? 1-5 07:00-17:00
    void getServiceUpTime_BasicRule() {
        //Arrange
        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1747135407286.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        //Situation1:Normal work days, created_at >= Tuesday 2025-04-01T07:00:00' AND created_at  < Wednesday  2025-04-02T07:00:00
        //Rules set up : add basic work rule
        Map<String, String> namesAndRules = Map.ofEntries(
                Map.entry("Normal work days", "??.??.???? ? 1-5 07:00-17:00"));


        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(rules.get(i))
                    .rule(namesAndRules.get(rules.get(i)));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }
        //add rules to already created group
        savedOHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);
        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 1.1
        from = "2025-04-01T07:00:00";
        to = "2025-04-02T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 1.2 having down time
        from = "2025-04-07T15:00:00";
        to = "2025-04-08T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.2
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.3Normal work days Monday 2025-03-31T07:00:00' to Tuesday  2025-04-02T07:00:00 - No down time
        from = "2025-03-31T07:00:00";
        to = "2025-04-02T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1200"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1200"));

        //Act 1.4Normal work days Wednesday 2025-04-02T12:00:00' to Thursday  2025-04-03T07:00:00
        //Downtime 2025-04-02T13:00:00 - 2025-04-02T14:00:00
        from = "2025-04-02T12:00:00";
        to = "2025-04-03T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.4
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("300"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("240"));

        //Act 1.5Normal work days Monday 2025-04-07T15:00:00' to Tuesday  2025-04-08T07:00:00
        //Downtime 2025-04-07 15:00:00 to = 2025-04-08 11:00:00
        from = "2025-04-07T15:00:00";
        to = "2025-04-08T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.5
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.6Normal work days Monday 2025-04-07T15:00:00' to Tuesday  2025-04-08T15:00:00
        //Downtime 2025-04-07 15:00:00 to = 2025-04-08 11:00:00
        from = "2025-04-07T15:00:00";
        to = "2025-04-08T15:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.6
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("240"));

        //Act 1.7Normal work days Monday 2025-04-07T15:00:00' to Wednesday  2025-04-09T07:00:00
        //Downtime 2025-04-07 15:00:00 to = 2025-04-08 11:00:00
        from = "2025-04-07T15:00:00";
        to = "2025-04-09T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.7
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("720"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("360"));

        //Act 1.8Normal work days Tuesday 2025-03-25T07:00:00' to Tuesday  2025-03-25T18:00:00
        //No downtime
        from = "2025-03-25T07:00:00";
        to = "2025-03-25T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.8
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 1.9Normal work days Tuesday 2025-03-25T07:00:00' to Wednesday 2025-03-26T08:00:00
        //No downtime
        from = "2025-03-25T07:00:00";
        to = "2025-03-26T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.9
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("660"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("660"));

        //Act 1.10Normal work days Tuesday 2025-03-25T17:00:00' to Wednesday 2025-03-26T08:00:00
        //No downtime
        from = "2025-03-25T17:00:00";
        to = "2025-03-26T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.10
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.11Normal work days Thursday 2025-03-27T10:00:00' to Thursday 2025-03-27T12:00:00
        //downtime Thursday "2025-03-27 11:00:00" to "2025-03-27 12:00:00"
        from = "2025-03-27T10:00:00";
        to = "2025-03-27T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.11
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.12Normal work days Thursday 2025-03-27T11:00:00' to Thursday 2025-03-27T12:00:00
        //downtime Thursday "2025-03-27 11:00:00" to "2025-03-27 12:00:00"
        from = "2025-03-27T11:00:00";
        to = "2025-03-27T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.12
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.13Normal work days Thursday 2025-03-27T10:00:00' to Thursday 2025-03-27T11:00:00
        //downtime Thursday "2025-03-27 11:00:00" to "2025-03-27 12:00:00"
        from = "2025-03-27T10:00:00";
        to = "2025-03-27T11:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.13
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.14Normal work days Friday 2025-04-11T06:00:00' to Friday 2025-04-11T18:00:00
        //No downtime
        from = "2025-04-11T06:00:00";
        to = "2025-04-11T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.14
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 1.15Normal work days Friday 2025-04-11T16:00:00' to Monday 2025-04-14T08:00:00
        //No downtime
        from = "2025-04-11T16:00:00";
        to = "2025-04-14T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.15
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("120"));

        //Act 1.16Normal work days Friday 2025-04-11T17:00:00' to Monday 2025-04-14T07:00:00
        //No downtime
        from = "2025-04-11T17:00:00";
        to = "2025-04-14T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.16
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.17Normal work days Friday 2025-04-11T17:00:00' to Monday 2025-04-14T08:00:00
        //No downtime
        from = "2025-04-11T17:00:00";
        to = "2025-04-14T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.17
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.18Normal work days Friday 2025-04-11T16:00:00' to Monday 2025-04-14T07:00:00
        //No downtime
        from = "2025-04-11T16:00:00";
        to = "2025-04-14T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.18
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.19Normal work days Friday 2025-04-11T16:00:00' to Saturday 2025-04-12T17:00:00
        //No downtime
        from = "2025-04-11T16:00:00";
        to = "2025-04-12T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.19
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.20Normal work days Friday 2025-04-11T16:00:00' to Saturday 2025-04-12T17:00:00
        //No downtime
        from = "2025-04-11T16:00:00";
        to = "2025-04-12T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.20
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.21Normal work days Friday 2025-04-11T17:00:00' to Saturday 2025-04-12T17:00:00
        //No downtime
        from = "2025-04-11T17:00:00";
        to = "2025-04-12T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.21
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.22Normal work days Monday 2025-04-14T07:00:00' to Monday 2025-04-12T08:00:00
        //No downtime
        from = "2025-04-14T07:00:00";
        to = "2025-04-14T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.22
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.23Normal work days Friday 2025-04-11T17:00:00' to Monday 2025-04-14T17:00:00
        //No downtime
        from = "2025-04-11T17:00:00";
        to = "2025-04-14T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.21
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 1.24Normal work days Friday 2025-04-11T17:00:00' to Friday 2025-04-11T18:00:00
        //No downtime
        from = "2025-04-11T17:00:00";
        to = "2025-04-11T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.24
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.25Normal work days Friday 2025-04-11T17:00:00' to Friday 2025-04-11T22:00:00
        //No downtime
        from = "2025-04-11T17:00:00";
        to = "2025-04-11T22:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.25
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.26Normal work days Saturday 2025-04-12T07:00:00' to Saturday 2025-04-12T17:00:00
        //No downtime
        from = "2025-04-12T07:00:00";
        to = "2025-04-12T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.26
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.27Normal work days Sunday 2025-04-13T07:00:00' to Sunday 2025-04-13T17:00:00
        //No downtime
        from = "2025-04-13T07:00:00";
        to = "2025-04-13T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.27
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.28Normal work days Monday 2025-04-14T06:00:00' to Monday 2025-04-14T07:00:00
        //No downtime
        from = "2025-04-14T06:00:00";
        to = "2025-04-14T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.28
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 1.29Normal work days Monday 2025-04-14T06:00:00' to Monday 2025-04-14T08:00:00
        //No downtime
        from = "2025-04-14T06:00:00";
        to = "2025-04-14T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.29
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 1.30Normal work days Monday 2025-04-14T06:00:00' to Monday 2025-04-14T17:00:00
        //No downtime
        from = "2025-04-14T06:00:00";
        to = "2025-04-14T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.30
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 1.31Normal work days Monday 2025-04-09T00:00:00' to Monday 2025-04-09T17:00:00
        //No downtime
        from = "2025-04-09T00:00:00";
        to = "2025-04-09T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.31
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 1.31Normal work days Wednesday 2024-01-01T06:00:00' to Wednesday 2024-12-31T17:00:00
        //No downtime
        /*from = "2024-01-01T06:00:00";
        to = "2024-12-31T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.31
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("157200"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("156840"));*/
    }

    //Normal weekend rule (Saturday and sunday): ??.??.???? ? 6-7 10:00-16:00
    @Test
    void getServiceUpTime_BasicRuleSaturdayToSunday() {
        //Arrange
        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1747135407286.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        //Situation1:Basic rule applicable for weekend only eg Saturday 2025-04-12T10:00:00' AND created_at  < Sunday 2025-04-13T16:00:00
        //Rules set up : add basic work rule
        Map<String, String> namesAndRules = Map.ofEntries(
                Map.entry("Normal weekends only", "??.??.???? ? 6-7 10:00-16:00"));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (String s : rules) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(s)
                    .rule(namesAndRules.get(s));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }
        //add rules to already created group
        savedOHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);
        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 2.1Saturday and Sunday only - Tuesday 2025-04-08T11:00:00' to Friday 2025-04-11T17:00:00
        //No downtime
        from = "2025-04-08T11:00:00";
        to = "2025-04-11T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 2.2Saturday 2025-04-05T07:00:00' to Sunday 2025-04-06T17:00:00
        //Downtime Friday 2025-04-04T15:00:00 to Saturday 2025-04-05 11:00:00
        from = "2025-04-05T07:00:00";
        to = "2025-04-06T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.2
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("720"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("660"));

        //Act 2.3Friday 2025-04-04T07:00:00' to Sunday 2025-04-06T17:00:00
        //Downtime Friday 2025-04-04T15:00:00 to Saturday 2025-04-05 11:00:00
        from = "2025-04-04T07:00:00";
        to = "2025-04-06T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("720"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("660"));

        //Act 2.4Saturday 2025-04-05T09:00:00' to Sunday 2025-04-06T07:00:00
        //Downtime Friday 2025-04-04T15:00:00 to Saturday 2025-04-05 11:00:00
        from = "2025-04-05T09:00:00";
        to = "2025-04-06T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.4
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("360"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("300"));

        //Act 2.5Friday 2025-04-04T14:00:00' to Saturday 2025-04-05T12:00:00
        //Downtime Friday 2025-04-04T15:00:00 to Saturday 2025-04-05 11:00:00
        from = "2025-04-04T14:00:00";
        to = "2025-04-05T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.5
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 2.6Saturday 2025-04-05T12:00:00' to Sunday 2025-04-06T07:00:00
        //No downtime
        from = "2025-04-05T12:00:00";
        to = "2025-04-06T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.6
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("240"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("240"));

        //Act 2.7Saturday 2025-04-05T12:00:00' to Saturday 2025-04-05T14:00:00
        //No downtime
        from = "2025-04-05T12:00:00";
        to = "2025-04-05T14:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.7
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("120"));

        //Act 2.8 Saturday 2025-04-05T16:00:00' to Sunday 2025-04-06T07:00:00
        //No downtime
        from = "2025-04-05T16:00:00";
        to = "2025-04-06T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.8
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 2.9 Saturday 2025-04-05T16:00:00' to Sunday 2025-04-06T16:00:00
        //No downtime
        from = "2025-04-05T16:00:00";
        to = "2025-04-06T16:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.9
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("360"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("360"));

        //Act 2.10 Saturday 2025-04-05T16:00:00' to Monday 2025-04-07T12:00:00
        //No downtime
        from = "2025-04-05T16:00:00";
        to = "2025-04-07T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.10
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("360"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("360"));

        //Act 2.11 Sunday 2025-04-06T16:00:00' to Monday 2025-04-07T12:00:00
        //No downtime
        from = "2025-04-06T16:00:00";
        to = "2025-04-07T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.11
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 2.12 Sunday 2025-04-06T15:00:00' to Monday 2025-04-07T12:00:00
        //No downtime
        from = "2025-04-06T15:00:00";
        to = "2025-04-07T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.12
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 2.13Friday 2025-04-04T15:00:00' to Sunday 2025-04-06T12:00:00
        //Downtime Friday 2025-04-04T15:00:00 to Saturday 2025-04-05 11:00:00
        from = "2025-04-04T15:00:00";
        to = "2025-04-06T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.13
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("480"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("420"));

        //Act 2.14Saturday 2025-04-05T10:00:00' to Sunday 2025-04-06T16:00:00
        //Downtime Friday 2025-04-04T15:00:00 to Saturday 2025-04-05 11:00:00
        from = "2025-04-05T10:00:00Z";
        to = "2025-04-06T16:00:00Z";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.14
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("720"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("660"));

        //Act 2.15Saturday 2025-04-05T10:00:00' to Saturday 2025-04-05T16:00:00
        //Downtime Friday 2025-04-04T15:00:00 to Saturday 2025-04-05 11:00:00
        from = "2025-04-05T10:00:00";
        to = "2025-04-05T16:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 2.7
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("360"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("300"));

    }

    @Test
        //Work day rule for Monday and Friday only: ??.??.???? ? 1,5 07:00-17:00
    void getServiceUpTime_BasicRuleMondayAndFridayOnly() {
        //Arrange
        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1747135407286.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        //Work day rule for Monday and Friday only
        //Rules set up : add Work day rule for Monday and Friday only:
        Map<String, String> namesAndRules = Map.ofEntries(
                Map.entry("Basic Monday and Friday only", "??.??.???? ? 1,5 07:00-17:00"));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (String s : rules) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(s)
                    .rule(namesAndRules.get(s));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }
        //add rules to already created group
        savedOHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);
        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 3.1Work days Tuesday 2025-04-08T11:00:00' to Thursday 2025-04-10T17:00:00
        //No downtime
        from = "2025-04-08T11:00:00";
        to = "2025-04-10T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 3.2Work days Monday 2025-04-07T11:00:00' to Thursday 2025-04-11T17:00:00
        //Downtime = Monday 2025-04-07 15:00:00 – Tuesday 2025-04-08 11:00:00
        from = "2025-04-07T11:00:00";
        to = "2025-04-11T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.2
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("960"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("840"));

        //Act 3.3Work days Monday 2025-04-07T11:00:00' and Tuesday 2025-04-08T07:00:00
        //Downtime = Monday 2025-04-07 15:00:00 – Tuesday 2025-04-08 11:00:00
        from = "2025-04-07T11:00:00";
        to = "2025-04-08T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("360"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("240"));

        //Act 3.4Work days Monday 2025-04-07T07:00:00' to Monday 2025-04-07T17:00:00
        //Downtime = Monday 2025-04-07 15:00:00 – Tuesday 2025-04-08 11:00:00
        from = "2025-04-07T07:00:00";
        to = "2025-04-07T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.4
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("480"));

        //Act 3.5Work days Monday 2025-04-07T06:00:00' to Monday 2025-04-07T14:00:00
        //Downtime = Monday 2025-04-07 15:00:00 – Tuesday 2025-04-08 11:00:00
        from = "2025-04-07T06:00:00";
        to = "2025-04-07T14:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.5
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("420"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("420"));

        //Act 3.6Work days Sunday 2025-04-06T06:00:00' to Monday 2025-04-07T17:00:00
        //Downtime = Monday 2025-04-07 15:00:00 – Tuesday 2025-04-08 11:00:00
        from = "2025-04-06T06:00:00";
        to = "2025-04-07T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.6
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("480"));

        //Act 3.7Work days Sunday 2025-04-06T17:00:00' to Monday 2025-04-07T08:00:00
        //No downtime
        from = "2025-04-06T17:00:00";
        to = "2025-04-07T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.7
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 3.8Work days Monday 2025-04-07T15:00:00' to Tuesday 2025-04-08T12:00:00
        //Downtime = Monday 2025-04-07 15:00:00 – Tuesday 2025-04-08 11:00:00
        from = "2025-04-07T15:00:00";
        to = "2025-04-08T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.8
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 3.9Work days Thursday 2025-04-10T15:00:00' to Friday 2025-04-11T08:00:00
        //No downtime
        from = "2025-04-10T15:00:00";
        to = "2025-04-11T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.9
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 3.10Work days Friday 2025-04-11T06:00:00' to Friday 2025-04-11T18:00:00
        //No downtime
        from = "2025-04-11T06:00:00";
        to = "2025-04-11T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.10
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 3.11Work days Friday 2025-04-11T18:00:00' to Saturday 2025-04-12T18:00:00
        //No downtime
        from = "2025-04-11T18:00:00";
        to = "2025-04-12T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.11
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 3.12Work days Friday 2025-04-11T12:00:00' to Saturday 2025-04-12T18:00:00
        //No downtime
        from = "2025-04-11T12:00:00";
        to = "2025-04-12T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.12
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("300"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("300"));

        //Act 3.13Work days Friday 2025-04-11T16:00:00' to Monday 2025-04-14T07:00:00
        //No downtime
        from = "2025-04-11T16:00:00";
        to = "2025-04-14T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.13
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 3.14Work days Friday 2025-04-11T16:00:00' to Monday 2025-04-14T08:00:00
        //No downtime
        from = "2025-04-11T16:00:00";
        to = "2025-04-14T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.14
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("120"));

        //Act 3.15Work days Friday 2025-04-11T07:00:00' to Monday 2025-04-14T08:00:00
        //No downtime
        from = "2025-04-11T17:00:00";
        to = "2025-04-14T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.15
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 3.16Work days Friday 2025-04-11T07:00:00' to Monday 2025-04-14T17:00:00
        //No downtime
        from = "2025-04-11T07:00:00";
        to = "2025-04-14T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.16
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1200"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1200"));

        //Act 3.17Work days Monday 2025-04-14T06:00:00' to Tuesday 2025-04-15T17:00:00
        //No downtime
        from = "2025-04-14T06:00:00";
        to = "2025-04-15T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.17
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 3.18Work days Tuesday 2025-04-15T06:00:00' to Tuesday 2025-04-15T17:00:00
        //No downtime
        from = "2025-04-15T06:00:00";
        to = "2025-04-15T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.18
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 3.19Work days Wednesday 2025-04-16T06:00:00' to Wednesday 2025-04-16T17:00:00
        //No downtime
        from = "2025-04-16T06:00:00";
        to = "2025-04-16T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 3.19
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));
    }

    @Test
        //Maintenance rule: 01.04.2025 ? ? 10:00-12:00
        // Work day rule Monday and Friday only: ??.??.???? ? 1,5 07:00-17:00
    void getServiceUpTime_MaintenanceAndWorkingWeek() {
        //Arrange
        //Rules set up : add maintenence rule: Early Closing Spring and
        // Work day rule: Normal Work days
        Map<String, String> namesAndRules = Stream.of(
                Map.entry("Early Closing Spring", "01.04.2025 ? ? 10:00-12:00"),
                Map.entry("Normal Work days", "??.??.???? ? 1-5 07:00-17:00")
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                LinkedHashMap::new
        ));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (String s : rules) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(s)
                    .rule(namesAndRules.get(s));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }

        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        oHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1747135407286.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 4.1 Maintenance 01.04.2025 ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        // Monday 2025-03-31T17:00:00' to Tuesday 2025-04-01T17:00:00
        //No downtime
        from = "2025-03-31T17:00:00";
        to = "2025-04-01T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 4.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("120"));

        //Act 4.2 Maintenance 01.04.2025 ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        // Monday 2025-03-31T07:00:00' to Tuesday 2025-04-01T17:00:00
        //No downtime
        from = "2025-03-31T07:00:00";
        to = "2025-04-01T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 4.2
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("720"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("720"));

        //Act 4.3 Maintenance 01.04.2025 ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        // Tuesday 2025-04-01T07:00:00' to Tuesday 2025-04-01T17:00:00
        //No downtime
        from = "2025-04-01T07:00:00";
        to = "2025-04-01T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 4.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("120"));

        //Act 4.4 Maintenance 01.04.2025 ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        // Tuesday 2025-04-01T12:00:00' to Tuesday 2025-04-01T17:00:00
        //No downtime
        from = "2025-04-01T12:00:00";
        to = "2025-04-01T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 4.4
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 4.5 Maintenance 01.04.2025 ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        // Tuesday 2025-04-01T12:00:00' to Wednesday 2025-04-02T08:00:00
        //No downtime
        from = "2025-04-01T12:00:00";
        to = "2025-04-02T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 4.5
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 4.6 Maintenance 01.04.2025 ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        // Tuesday 2025-04-01T12:00:00' to Wednesday 2025-04-02T17:00:00
        //Wednesday 2025-04-02 13:00:00 to Wednesday 2025-04-02 14:00:00
        from = "2025-04-01T12:00:00";
        to = "2025-04-02T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 4.6
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("540"));

        //Act 4.7 Maintenance 01.04.2025 ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        // Friday 2025-03-28T12:00:00' to Wednesday 2025-04-02T08:00:00
        //Downtime:
        //Friday 2025-03-28 13:00:00 to Friday 2025-03-28 14:00:00
        //Sunday 2025-03-30 11:00:00 to Sunday 2025-03-30 12:00:00
        //Wednesday 2025-04-02 13:00:00 to Wednesday 2025-04-02 14:00:00
        from = "2025-03-28T12:00:00";
        to = "2025-04-02T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 4.7
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1620"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1500"));

        //Act 4.8 Maintenance 01.04.2025 ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Sunday2025-03-30 07:00:00 to Wednesday 2025-04-02 17:00:00
        //Downtime:
        //Sunday 2025-03-30 11:00:00 to Sunday 2025-03-30 12:00:00
        //Wednesday 2025-04-02 13:00:00 to Wednesday 2025-04-02 14:00:00
        from = "2025-03-30T07:00:00";
        to = "2025-04-02T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 4.8
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1320"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1260"));
    }

    @Test
        //Rules: Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        //Easter Monday 21.04.2025 ? ? 00:00-00:00
        //Basic weekend group:  ??.??.???? ? 1-5 07:00-17:00
    void getServiceUpTime_Easter2025AndWorkingWeek() {
        //Arrange
        //Rules set up :
        // Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        Map<String, String> namesAndRules = Stream.of(
                Map.entry("Maundy Thursday", "17.04.2025 ? ? 09:00-12:00"),
                Map.entry("Good Friday", "18.04.2025 ? ? 00:00-00:00"),
                Map.entry("Easter Monday", "21.04.2025 ? ? 00:00-00:00"),
                Map.entry("Normal Work days", "??.??.???? ? 1-5 07:00-17:00")
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                LinkedHashMap::new
        ));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (String s : rules) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(s)
                    .rule(namesAndRules.get(s));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }

        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        oHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1747135407286.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        // Act 5.1 Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-16T07:00:00 to Tuesday 2025-04-22T07:00:00
        //No downtime
        from = "2025-04-16T07:00:00";
        to = "2025-04-22T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 5.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("780"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("780"));

        // Act 5.2 Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-16T17:00:00 to Tuesday 2025-04-22T07:00:00
        //No downtime
        from = "2025-04-16T17:00:00";
        to = "2025-04-22T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 5.2
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("180"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("180"));

        // Act 5.3 Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        //Monday 2025-04-14T06:00:00 to Tuesday 2025-04-22T07:00:00
        //No downtime
        from = "2025-04-14T06:00:00";
        to = "2025-04-22T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 5.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1980"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1980"));

        // Act 5.4 Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        //Monday 2025-04-14T06:00:00 to Tuesday 2025-04-22T07:00:00
        //No downtime
        from = "2025-04-14T06:00:00";
        to = "2025-04-22T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 5.4
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("2040"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("2040"));

    }

    @Test
        //Easter maintenance 16.04.2025 ? ? 07:00-14:00
        //Rules: Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        //Good Friday 18.04.2025 ? ? 00:00-00:00,
        //Easter Monday 21.04.2025 ? ? 00:00-00:00
        //Basic weekend group:  ??.??.???? ? 1-5 07:00-17:00
    void getServiceUpTime_MaintenanceEaster2025AndWorkingWeek() {
        //Arrange
        // Rules set up :
        // Easter maintenance 16.04.2025 ? ? 07:00-14:00
        // Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        Map<String, String> namesAndRules = Stream.of(
                Map.entry("Easter maintenance", "16.04.2025 ? ? 07:00-14:00"),
                Map.entry("Maundy Thursday", "17.04.2025 ? ? 09:00-12:00"),
                Map.entry("Good Friday", "18.04.2025 ? ? 00:00-00:00"),
                Map.entry("Easter Monday", "21.04.2025 ? ? 00:00-00:00"),
                Map.entry("Normal Work days", "??.??.???? ? 1-5 07:00-17:00")
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                LinkedHashMap::new
        ));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (String s : rules) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(s)
                    .rule(namesAndRules.get(s));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }

        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        oHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1747135407286.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        // Act 6.1
        // Easter maintenance 16.04.2025 ? ? 07:00-14:00
        // Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-16T07:00:00 to Tuesday 2025-04-22T07:00:00
        //No downtime
        from = "2025-04-16T07:00:00";
        to = "2025-04-22T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 6.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        // Act 6.2
        // Easter maintenance 16.04.2025 ? ? 07:00-14:00
        // Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-16T07:00:00 to Tuesday 2025-04-22T17:00:00
        //No downtime
        from = "2025-04-16T07:00:00";
        to = "2025-04-22T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 6.2
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1200"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1200"));

        // Act 6.3
        // Easter maintenance 16.04.2025 ? ? 07:00-14:00
        // Easter Maundy Thursday 17.04.2025 ? ? 09:00-12:00,
        // Good Friday 18.04.2025 ? ? 00:00-00:00,
        // Easter Monday 21.04.2025 ? ? 00:00-00:00
        // Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-16T07:00:00 to Tuesday 2025-04-22T12:00:00
        //No downtime
        from = "2025-04-16T07:00:00";
        to = "2025-04-22T12:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 6.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("900"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("900"));
    }

    @Test
        //Mayday rule: 01.05.???? ? ? 00:00-00:00
        //Work day rule Monday and Friday only: ??.??.???? ? 1,5 07:00-17:00
    void getServiceUpTime_MaydayAndWorkingWeek() {
        //Arrange
        //Rules set up : add Mayday - closed all day
        //Work day rule: Normal Work days
        Map<String, String> namesAndRules = Stream.of(
                Map.entry("May day", "01.05.???? ? ? 00:00-00:00"),
                Map.entry("Normal Work days", "??.??.???? ? 1-5 07:00-17:00")
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                LinkedHashMap::new
        ));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (String s : rules) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(s)
                    .rule(namesAndRules.get(s));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }

        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        oHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1747135407286.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 7.1 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-30T23:00:00' to Friday 2025-04-02T07:00:00
        //No downtime
        from = "2025-04-30T23:00:00";
        to = "2025-05-02T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 7.2 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Thursday 2025-05-01T01:00:00' to Friday 2025-04-02T07:00:00
        //No downtime
        from = "2025-05-01T01:00:00";
        to = "2025-05-02T07:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.2
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 7.3 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Thursday 2025-05-01T07:00:00' to Thursday 2025-04-01T18:00:00
        //No downtime
        from = "2025-05-01T07:00:00";
        to = "2025-05-01T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("0"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("0"));

        //Act 7.4 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Thursday 2025-05-01T16:00:00' to Friday 2025-04-02T18:00:00
        //Downtime Friday 2025-05-02 15:00:00+02 to Saturday 2025-05-03 11:00:00+02
        from = "2025-05-01T16:00:00";
        to = "2025-05-02T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.4
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("480"));

        //Act 7.5 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-30T00:00:00' to Thursday 2025-04-01T17:00:00
        //No downtime
        from = "2025-04-30T00:00:00";
        to = "2025-05-01T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.5
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 7.6 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-30T00:00:00' to Thursday 2025-04-02T00:00:00
        //No downtime
        from = "2025-04-30T00:00:00";
        to = "2025-05-02T00:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.5
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 7.7 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Wednesday 2025-04-30T00:00:00' to Saturday 2025-04-03T00:00:00
        //Downtime Friday 2025-05-02 15:00:00+02 to Saturday 2025-05-03 11:00:00+02
        from = "2025-04-30T00:00:00";
        to = "2025-05-03T00:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.7
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1200"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1080"));

        //Act 7.8 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Thursday 2025-05-01T00:00:00' to Friday 2025-04-02T08:00:00
        //Downtime Friday 2025-05-02 15:00:00+02 to Saturday 2025-05-03 11:00:00+02
        from = "2025-05-01T00:00:00";
        to = "2025-05-02T08:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.7
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("60"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("60"));

        //Act 7.8 Mayday 01.05.???? ? ? 10:00-12:00  and Basic working week Monday to Friday 07:00-17:00
        //Thursday 2025-05-01T12:30:00' to Friday 2025-04-02T17:00:00
        //Downtime Friday 2025-05-02 15:00:00+02 to Saturday 2025-05-03 11:00:00+02
        from = "2025-05-01T12:30:00";
        to = "2025-05-02T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 7.7
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("480"));

    }

    @Test
        //Open all times all days: ??.??.???? ? 1-7 00:00-23:59
    void getServiceUpTime_OpenAllTimesAndDays() {
        //Arrange
        //Rules set up : add Mayday - closed all day
        //Work day rule: Normal Work days
        Map<String, String> namesAndRules = Stream.of(
                Map.entry("Open all days", "??.??.???? ? 1-7 00:00-23:59")
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                LinkedHashMap::new
        ));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (String s : rules) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(s)
                    .rule(namesAndRules.get(s));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }

        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        oHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1747135407286.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 8.1 Open all Times and Days ??.??.???? ? 1-7 00:00-23:59
        //Monday 2025-05-12T00:00:00 to Tuesday2025-05-13T06:00:00
        //No downtime
        from = "2025-05-12T00:00:00";
        to = "2025-05-13T06:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 8.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1800"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1800"));

        //Act 8.2 Open all Times and Days ??.??.???? ? 1-7 00:00-23:59
        //Monday 2025-05-12T00:00:00 to Tuesday2025-05-13T00:00:00
        //No downtime
        from = "2025-05-12T00:00:00";
        to = "2025-05-13T00:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 8.2
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1440"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1440"));

        //Act 8.3 Open all Times and Days ??.??.???? ? 1-7 00:00-23:59
        //Monday 2025-05-12T00:00:00 to Tuesday2025-05-13T00:00:00
        //No downtime
        from = "2025-05-12T23:00:00";
        to = "2025-05-13T01:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 8.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("120"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("120"));

        //Act 8.4 Open all Times and Days ??.??.???? ? 1-7 00:00-23:59
        //Monday 2025-05-12T00:00:00 to Monday 2025-05-12T23:59:00
        //No downtime
        from = "2025-05-12T00:00:00";
        to = "2025-05-12T23:59:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 8.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1440"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1440"));

        //Act 8.4 Open all Times and Days ??.??.???? ? 1-7 00:00-23:59
        //Wednesday 2025-05-12T00:00:00 to Wednesday 2025-05-14T23:59:00
        //downtime 2025-05-14 13:00:00 to 2025-05-14 14:00:00
        from = "2025-05-14T00:00:00";
        to = "2025-05-14T23:59:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 8.3
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1440"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1380"));
    }

    @Test
        //Normal work day rule: ??.??.???? ? 1-5 07:00-17:00
    void getServiceUpTime2024_BasicRule() {
        //Arrange
        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1751875240840.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        //Situation1:Normal work days, created_at >= Tuesday 2024-01-01T07:00:00' AND created_at  < Wednesday  2024-12-31T18:00:00
        //Rules set up : add basic work rule
        Map<String, String> namesAndRules = Map.ofEntries(
                Map.entry("Normal work days", "??.??.???? ? 1-5 07:00-17:00"));


        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(rules.get(i))
                    .rule(namesAndRules.get(rules.get(i)));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }

        //add rules to already created group
        savedOHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);
        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 9.1
        //downtime:
        // Saturday 2024-01-19 19:00:00 to Monday 2024-01-22 05:00:00 = 0
        //Tuesday 2024-01-23 08:00:00 to Tuesday 2024-01-23 10:00:00 = 120
        from = "2024-01-01T07:00:00";
        to = "2024-01-31T18:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("13800"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("13680"));

        //Act 9.2
        //Downtime:
        // Saturday 2024-01-19 19:00:00 to Monday 2024-01-22 05:00:00 = 0
        //Tuesday 2024-01-23 08:00:00 to Tuesday 2024-01-23 10:00:00 = 120
        //Saturday 2024-10-12 17:00:00 to Sunday 2024-10-13 10:00 = 0
        //Tuesday 2024-10-29 10:00:00 to Tuesday 2024-10-29 14:00:00 = 240
        from = "2024-01-01T06:00:00";
        to = "2024-12-31T17:00:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("157200"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("156840"));
    }

    @Test
        //Normal work day rule: ??.??.???? ? 1-5 07:00-17:00
    void getServiceUpTime2024_HolidaysAndBasicRule() {
        //Arrange
        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1751875240840.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        //Situation1:2024 holidays  Normal work days, created_at >= Tuesday 2024-01-01T07:00:00' AND created_at  < Wednesday  2024-12-31T18:00:00
        //Rules set up : add holidays and basic work rule

        Map<String, String> namesAndRules = Stream.of(
                Map.entry("Maundy Thursday", "28.03.2024 ? ? 07:00-12:00"),
                Map.entry("Good Friday", "29.03.2024 ? ? 00:00-00:00"),
                Map.entry("Easter Monday", "01.04.2024 ? ? 00:00-00:00"),
                Map.entry("Kristi himmelsfartsdag", "09.05.2024 ? ? 00:00-00:00"),
                Map.entry("Annen Pinse dag", "20.05.2024 ? ? 00:00-00:00"),
                Map.entry("New years day", "01.01.???? ? ? 00:00-00:00"),
                Map.entry("May day", "01.05.???? ? ? 00:00-00:00"),
                Map.entry("National day", "17.05.???? ? ? 00:00-00:00"),
                Map.entry("Christmas eve", "24.12.???? ? ? 00:00-00:00"),
                Map.entry("Christmas day", "25.12.???? ? ? 00:00-00:00"),
                Map.entry("Boxing day", "26.12.???? ? ? 00:00-00:00"),
                Map.entry("Normal Work days", "??.??.???? ? 1-5 07:00-17:00")
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                LinkedHashMap::new
        ));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(rules.get(i))
                    .rule(namesAndRules.get(rules.get(i)));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }

        //add rules to already created group
        savedOHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);
        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 10.1 Monday 2024-01-01T00:00:00 to Tuesday 2024-01-02T23:59:00
        //Downtime:
        // Saturday 2024-01-19 19:00:00 to Monday 2024-01-22 05:00:00 = 0
        //Tuesday 2024-01-23 08:00:00 to Tuesday 2024-01-23 10:00:00 = 120
        //Saturday 2024-10-12 17:00:00 to Sunday 2024-10-13 10:00 = 0
        //Tuesday 2024-10-29 10:00:00 to Tuesday 2024-10-29 14:00:00 = 240
        from = "2024-01-01T00:00:00";
        to = "2024-01-02T23:59:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("600"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("600"));

        //Act 10.2 Monday 2024-01-01T00:00:00 to Wednesday 2024-01-31T23:59:00
        //Downtime:
        // Saturday 2024-01-19 19:00:00 to Monday 2024-01-22 05:00:00 = 0
        //Tuesday 2024-01-23 08:00:00 to Tuesday 2024-01-23 10:00:00 = 120
        //Saturday 2024-10-12 17:00:00 to Sunday 2024-10-13 10:00 = 0
        //Tuesday 2024-10-29 10:00:00 to Tuesday 2024-10-29 14:00:00 = 240
        from = "2024-01-01T00:00:00";
        to = "2024-01-31T23:59:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("13200"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("13080"));

        //Act 10.3 Monday 2024-01-01T00:00:00 to Tuesday 2024-12-31T23:59:00
        //Downtime:
        // Saturday 2024-01-19 19:00:00 to Monday 2024-01-22 05:00:00 = 0
        //Tuesday 2024-01-23 08:00:00 to Tuesday 2024-01-23 10:00:00 = 120
        //Saturday 2024-10-12 17:00:00 to Sunday 2024-10-13 10:00 = 0
        //Tuesday 2024-10-29 10:00:00 to Tuesday 2024-10-29 14:00:00 = 240
        from = "2024-01-01T00:00:00";
        to = "2024-12-31T23:59:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("150900"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("150540"));
    }

    @Test
        //Open all times all days : ??.??.???? ? 1-7 00:00-23:59
    void getServiceUpTime2024_HolidaysAndOpenAllDaysAndTimesRule() {
        //Arrange
        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //re-service service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("data-1751875240840.csv")).getPath();
        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
        );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        //Situation1:2024 holidays  Open all days and times, created_at >= Tuesday 2024-01-01T00:00:00' AND created_at  < Wednesday  2024-12-31T23:59:00
        //Rules set up : add holidays and open all days and time rules:

        Map<String, String> namesAndRules = Stream.of(
                Map.entry("Maundy Thursday", "28.03.2024 ? ? 07:00-12:00"),
                Map.entry("Good Friday", "29.03.2024 ? ? 00:00-00:00"),
                Map.entry("Easter Monday", "01.04.2024 ? ? 00:00-00:00"),
                Map.entry("Kristi himmelsfartsdag", "09.05.2024 ? ? 00:00-00:00"),
                Map.entry("Annen Pinse dag", "20.05.2024 ? ? 00:00-00:00"),
                Map.entry("New years day", "01.01.???? ? ? 00:00-00:00"),
                Map.entry("May day", "01.05.???? ? ? 00:00-00:00"),
                Map.entry("National day", "17.05.???? ? ? 00:00-00:00"),
                Map.entry("Christmas eve", "24.12.???? ? ? 00:00-00:00"),
                Map.entry("Christmas day", "25.12.???? ? ? 00:00-00:00"),
                Map.entry("Boxing day", "26.12.???? ? ? 00:00-00:00"),
                Map.entry("Open all times all days", "??.??.???? ? 1-7 00:00-23:59")
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                LinkedHashMap::new
        ));

        List<String> rules = new ArrayList<>(namesAndRules.keySet());
        List<OHRuleDto> selectedRules = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            OHRuleDto rule = new OHRuleDto()
                    .id(UUID.randomUUID())
                    .name(rules.get(i))
                    .rule(namesAndRules.get(rules.get(i)));
            selectedRules.add(rule);
            openingHoursController.newRule(rule);
        }

        //add rules to already created group
        savedOHGroupThinDto.setRules(selectedRules.stream().map(OHRuleDto::getId).toList());
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);
        String from, to;
        UpTimeTotalsDto retrievedUpTimeTotalsDto;

        //Act 11.1 Monday 2024-01-01T00:00:00 to Tuesday 2024-01-02T23:59:00
        //Downtime:
        // Saturday 2024-01-19 19:00:00 to Monday 2024-01-22 05:00:00 = 0
        //Tuesday 2024-01-23 08:00:00 to Tuesday 2024-01-23 10:00:00 = 120
        //Saturday 2024-10-12 17:00:00 to Sunday 2024-10-13 10:00 = 0
        //Tuesday 2024-10-29 10:00:00 to Tuesday 2024-10-29 14:00:00 = 240
        from = "2024-01-01T00:00:00";
        to = "2024-01-02T23:59:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("1440"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("1440"));

        //Act 11.2 Monday 2024-01-01T00:00:00 to Wednesday 2024-01-31T23:59:00
        //Downtime:
        // Saturday 2024-01-19 19:00:00 to Monday 2024-01-22 05:00:00 = 0
        //Tuesday 2024-01-23 08:00:00 to Tuesday 2024-01-23 10:00:00 = 120
        //Saturday 2024-10-12 17:00:00 to Sunday 2024-10-13 10:00 = 0
        //Tuesday 2024-10-29 10:00:00 to Tuesday 2024-10-29 14:00:00 = 240
        from = "2024-01-01T00:00:00";
        to = "2024-01-31T23:59:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("43200"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("39600"));

        //Act 11.3 Monday 2024-01-01T00:00:00 to Tuesday 2024-12-31T23:59:00
        //Downtime:
        // Saturday 2024-01-19 19:00:00 to Monday 2024-01-22 05:00:00 = 0
        //Tuesday 2024-01-23 08:00:00 to Tuesday 2024-01-23 10:00:00 = 120
        //Saturday 2024-10-12 17:00:00 to Sunday 2024-10-13 10:00 = 0
        //Tuesday 2024-10-29 10:00:00 to Tuesday 2024-10-29 14:00:00 = 240
        from = "2024-01-01T00:00:00";
        to = "2024-12-31T23:59:00";
        retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                from,
                to);
        //Assert 1.1
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfExpectedUptime())
                .isEqualTo(new BigDecimal("511500"));
        Assertions.assertThat(retrievedUpTimeTotalsDto.getSumOfActualUptime())
                .isEqualTo(new BigDecimal("506640"));
    }


}


