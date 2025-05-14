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
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UpTimeControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    private final UpTimeController upTimeController = new UpTimeController(dbContext);

    private final OpeningHoursController openingHoursController = new OpeningHoursController(dbContext);

    private final ServiceController serviceController = new ServiceController(dbContext);


    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
        TestUtil.clearAllTableData(dbContext);
    }

    @AfterEach
    void endConnection() {
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
        ZonedDateTime now = ZonedDateTime.now();
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
        ZonedDateTime now = ZonedDateTime.now();
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
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        //Act
        UpTimeTotalsDto retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId().toString(),
                LocalDateTime.now().minusDays(30).toString(),
                LocalDateTime.now().toString());


        //Assert
        System.out.println(retrievedUpTimeTotalsDto.toString());
    }

    @Test
    void getServiceUpTime_BasicRule() {
        //Arrange
        //Group set up --give random group name
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        //Create service
        //reservice service UUID for testing from csv file service Id aafc64ba-70a8-4ae4-896e-69306aab0ab4
        UUID reservedServiceUUID = UUID.fromString("aafc64ba-70a8-4ae4-896e-69306aab0ab4");
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        service.setId(reservedServiceUUID);
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(), serviceDto.getId());

        //Create records
        //march to april, 2025 from csv file.
        String filePath = getClass().getClassLoader().getResource("data-1747135407286.csv").getPath();

        List<RecordEntity> records = SampleDataDto.generateRecordEntitiesFromCSVFile(
                filePath,
                reservedServiceUUID
                );

        records.forEach(record -> {
            record.setId(TestUtil.saveRecordBackInTime(record, dbContext));
        });

        //Situation1:Normal work days, created_at >= Tuesday 2025-04-01T07:00:00' AND created_at  < Wednesday  2025-04-02T07:00:00
        //Rules set up : add basic work rule
        Map<String, String> namesAndRules =  Map.ofEntries(
                Map.entry("Normal work days", "??.??.???? ? 1-5 07:00-17:00"),
                Map.entry("National day","17.05.???? ? ? 00:00-00:00"));

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

        //Situation2:Normal work days plus weekend, created_at >= Tuesday 2025-04-01T07:00:00' AND created_at  < Wednesday  2025-04-02T07:00:00


        //Situation3:Normal work days plus weekend plus holiday created_at >= Tuesday 2025-04-01T07:00:00' AND created_at  < Wednesday  2025-04-02T07:00:00


    }

}
