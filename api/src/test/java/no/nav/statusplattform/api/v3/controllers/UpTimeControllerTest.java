package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.openingHours.TimeSpan;
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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

        //Group oppsett
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
        TimeSpan timeSpan = new TimeSpan(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now());

        UpTimeTotalsDto retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId(), timeSpan);

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

        //Group oppsett
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
        TimeSpan timeSpan = new TimeSpan(
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now());

        UpTimeTotalsDto retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId(), timeSpan);

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

        //Group oppsett
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
        TimeSpan timeSpan = new TimeSpan(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now());

        UpTimeTotalsDto retrievedUpTimeTotalsDto
                = upTimeController.getServiceUpTimeSums(serviceDto.getId(), timeSpan);

        //Assert
        System.out.println(retrievedUpTimeTotalsDto.toString());
    }

}
