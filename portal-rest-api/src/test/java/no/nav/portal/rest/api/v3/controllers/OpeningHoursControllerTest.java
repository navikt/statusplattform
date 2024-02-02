package no.nav.portal.rest.api.v3.controllers;


import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.SampleData;
import nav.portal.core.repositories.TestDataSource;
import nav.portal.core.repositories.TestUtil;
import no.nav.portal.rest.api.EntityDtoMappers;

import no.portal.web.generated.api.OHGroupDto;
import no.portal.web.generated.api.OHGroupThinDto;
import no.portal.web.generated.api.OHRuleDto;
import no.portal.web.generated.api.ServiceDto;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpeningHoursControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

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
    void getRules() {
        //Arrange
        List<OHRuleDto> oHRulesDto = SampleDataDto.getRulesDto();
        List<OHRuleDto>savedOHRulesDto = new ArrayList<>();
       List<UUID>savedOHRulesDtoIds = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
            savedOHRulesDtoIds.add(oHRuleDto.getId());
        });
        //Act
       List<OHRuleDto>retrievedOHRulesDto = openingHoursController.getRules();
       List<UUID>retrievedOHRulesDtoIds = new ArrayList<>();
       retrievedOHRulesDto.forEach(retrievedRuleDto->retrievedOHRulesDtoIds.add(retrievedRuleDto.getId()));
       //Assert
       Assertions.assertThat(retrievedOHRulesDto.size()).isEqualTo(savedOHRulesDto.size());
       Assertions.assertThat(retrievedOHRulesDtoIds).containsAll(savedOHRulesDtoIds);
    }

    @Test
    void newRule(){
        //Arrange
        OHRuleDto oHRuleDto = SampleDataDto.getRandomizedOHRuleDto();
        //Act
        OHRuleDto savedOHRuleDto = openingHoursController.newRule(oHRuleDto);
        oHRuleDto.setId(oHRuleDto.getId());
        OHRuleDto retrievedOHRuleDto = openingHoursController.getRule(oHRuleDto.getId());
        //Assert
        Assertions.assertThat(retrievedOHRuleDto).isEqualTo(oHRuleDto);
        Assertions.assertThat(retrievedOHRuleDto.getId()).isEqualTo(oHRuleDto.getId());
    }



    @Test
    void updateRule() {
        //Arrange
        List<OHRuleDto>oHRulesDtos = SampleDataDto.getNonEmptyListOfOHRuleDto(2);
        List<OHRuleDto>oHRulesDtoBefore = new ArrayList<>();
        oHRulesDtos.forEach(oHRuleDto -> {
            oHRulesDtoBefore.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
        });
        String nameBefore = oHRulesDtoBefore.get(0).getName();
        //Act
        oHRulesDtoBefore.get(0).setName(oHRulesDtoBefore.get(1).getName());
        openingHoursController.updateRule(oHRulesDtoBefore.get(0).getId(), oHRulesDtoBefore.get(1));
        List<OHRuleDto>oHRulesDtoAfter = openingHoursController.getRules();
        //Assert
        Assertions.assertThat(nameBefore).isNotEqualTo(oHRulesDtoBefore.get(1).getName());
        Assertions.assertThat(oHRulesDtoAfter.get(0).getName()).isEqualTo(oHRulesDtoAfter.get(1).getName());

    }

    @Test
    void deleteRule() {
        //Arrange
        List<OHRuleDto>oHRulesDtos = SampleDataDto.getNonEmptyListOfOHRuleDto(2);
        List<OHRuleDto>oHRulesDtoBefore = new ArrayList<>();
        oHRulesDtos.forEach(oHRuleDto -> {
            oHRulesDtoBefore.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
        });
        OHRuleDto ruleToBeDeleted = oHRulesDtoBefore.get(0);
        //Act
        openingHoursController.deleteRule(oHRulesDtoBefore.get(0).getId());
        List<OHRuleDto>oHRulesDtoAfter = openingHoursController.getRules();
        //Assert
        Assertions.assertThat(oHRulesDtoBefore.size()).isEqualTo(2);
        Assertions.assertThat(oHRulesDtoAfter.size()).isEqualTo(1);
        Assertions.assertThat(oHRulesDtoBefore).contains(ruleToBeDeleted);
        Assertions.assertThat(oHRulesDtoAfter).doesNotContain(ruleToBeDeleted);
    }

    @Test
    void getRule() {
        //Arrange
        List<OHRuleDto> oHRulesDto = SampleDataDto.getRulesDto();
        List<OHRuleDto>savedOHRulesDto = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
        });
        //Act
        OHRuleDto retrievedOHRuleDto = openingHoursController.getRule(savedOHRulesDto.get(0).getId());
        //Assert
        Assertions.assertThat(retrievedOHRuleDto).isEqualTo(savedOHRulesDto.get(0));
        Assertions.assertThat(retrievedOHRuleDto.getId()).isEqualTo(savedOHRulesDto.get(0).getId());
    }

    @Test
    void addRuleToGroup() {
        //Arrange
        OHRuleDto oHRuleDto = SampleDataDto.getRandomizedOHRuleDto();
        OHRuleDto savedOHRuleDto = openingHoursController.newRule(oHRuleDto);
        oHRuleDto.setId(oHRuleDto.getId());
        OHRuleDto retrievedOHRuleDto = openingHoursController.getRule(oHRuleDto.getId());

        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        savedOHGroupThinDto.setId(savedOHGroupThinDto.getId());


        UUID ruleId = oHRuleDto.getId();
        UUID groupId = savedOHGroupThinDto.getId();

        OHGroupDto retrievedGroupBefore = openingHoursController.getGroup(groupId);
        //Act
        List<UUID> rules = oHGroupThinDto.getRules();
        if (rules.size() == 0) {
            rules = new ArrayList<>();
        }
        rules.add(ruleId);
        oHGroupThinDto.setRules(rules);
        openingHoursController.updateGroup(groupId, oHGroupThinDto);
        OHGroupDto retrievedGroupAfter = openingHoursController.getGroup(groupId);
        //Assert
        OHGroupDto rule = retrievedGroupAfter.getRules().get(0);
        Assertions.assertThat(rule.getId()).isEqualTo(ruleId);
        Assertions.assertThat(rule.getRule()).isEqualTo(oHRuleDto.getRule());
    }

    @Test
    void getGroups() {
        //Arrange
        List<OHGroupThinDto> oHGroupsThinDto= SampleDataDto.getGroupsThinDto();
        List<OHGroupThinDto>groupsBefore = new ArrayList<>();
        List<UUID>groupsBeforeIds = new ArrayList<>();
        oHGroupsThinDto.forEach(oHGroupThinDto -> {
            groupsBefore.add(openingHoursController.newGroup(oHGroupThinDto));
            oHGroupThinDto.setId(oHGroupThinDto.getId());
            groupsBeforeIds.add(oHGroupThinDto.getId());
        });
        //Act
        List<OHGroupDto>retrievedGroups = openingHoursController.getGroups();
        List<UUID>retrievedGroupsIds = new ArrayList<>();
        retrievedGroups.forEach(retrievedGroup-> retrievedGroupsIds.add(retrievedGroup.getId()));
        //Assert
        Assertions.assertThat(retrievedGroups.size()).isEqualTo(groupsBefore.size());
        Assertions.assertThat(retrievedGroupsIds).containsAll(groupsBeforeIds);
    }

    @Test
    void newGroup(){
        //Arrange
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        savedOHGroupThinDto.setId(savedOHGroupThinDto.getId());
        UUID OHGroupThinDtoId = savedOHGroupThinDto.getId();
        //Act
        OHGroupDto retrievedOHGroupThinDto = openingHoursController.getGroup(savedOHGroupThinDto.getId());
        UUID retrievedOHGroupThinDtoId = retrievedOHGroupThinDto.getId();
        //Assert
        Assertions.assertThat(retrievedOHGroupThinDto.getName()).isEqualTo(savedOHGroupThinDto.getName());
        Assertions.assertThat(retrievedOHGroupThinDtoId).isEqualTo(OHGroupThinDtoId);
    }

    @Test
    void deleteGroup(){
        //Arrange
        OHGroupThinDto basicGroup = SampleDataDto.getBasicGroupThinDto();
        OHGroupThinDto savedBasicGroup = openingHoursController.newGroup(basicGroup);
        savedBasicGroup.setId(savedBasicGroup.getId());

        List<OHGroupThinDto> groups = SampleDataDto.getNonEmptyListOfOHGroupThinDto(2);
        List<OHGroupThinDto>savedGroups = new ArrayList<>();
        groups.forEach( group -> {
            savedGroups.add(openingHoursController.newGroup(group));
            group.setId(group.getId());
        });
        OHGroupThinDto groupToBeDeleted = savedGroups.get(0);
        List<UUID> savedGroupsId = new ArrayList<>();
        savedGroups.forEach(group-> savedGroupsId.add(group.getId()));
        basicGroup.setRules(savedGroupsId);
        openingHoursController.updateGroup(basicGroup.getId(),basicGroup);
        List<OHGroupDto> retrievedGroupsBefore = openingHoursController.getGroups();
        List<UUID> retrievedGroupsUUIDBefore = new ArrayList<>();
        retrievedGroupsBefore.forEach(retrievedGroupBefore ->retrievedGroupsUUIDBefore.add(retrievedGroupBefore.getId()));
        //Act
        openingHoursController.deleteGroup(groupToBeDeleted.getId());
        List<OHGroupDto> retrievedGroupsAfter = openingHoursController.getGroups();
        List<UUID> retrievedGroupsUUIDAfter = new ArrayList<>();
        retrievedGroupsAfter.forEach(retrievedGroupAfter ->retrievedGroupsUUIDAfter.add(retrievedGroupAfter.getId()));
        //Assert
        Assertions.assertThat(retrievedGroupsUUIDBefore).contains(groupToBeDeleted.getId());
        Assertions.assertThat(retrievedGroupsUUIDAfter).doesNotContain(groupToBeDeleted.getId());
    }

    @Test
    void addGroupToGroup() {
        //Arrange
        List<OHGroupThinDto> oHGroupsThinDto = SampleDataDto.getNonEmptyListOfOHGroupThinDto(2);
        OHGroupThinDto oHGroupThinDto1 = openingHoursController.newGroup(oHGroupsThinDto.get(0));
        OHGroupThinDto oHGroupThinDto2 = openingHoursController.newGroup(oHGroupsThinDto.get(1));
        oHGroupThinDto1.setId(oHGroupThinDto1.getId());
        oHGroupThinDto2.setId(oHGroupThinDto2.getId());
        OHGroupDto retrievedBefore = openingHoursController.getGroup(oHGroupThinDto1.getId());
        List<OHGroupDto> retrievedRulesBefore = retrievedBefore.getRules();
        //Act
        List<UUID> rules = oHGroupThinDto1.getRules();
        if (rules.size() == 0) {
            rules = new ArrayList<>();
        }
        rules.add(oHGroupThinDto2.getId());
        oHGroupThinDto1.setRules(rules);
        openingHoursController.updateGroup(oHGroupThinDto1.getId(),oHGroupThinDto1);
        OHGroupDto retrievedAfter = openingHoursController.getGroup(oHGroupThinDto1.getId());
        List<OHGroupDto> retrievedRulesAfter = retrievedAfter.getRules();
        OHGroupDto retrievedAddedGroup = retrievedRulesAfter.get(0);
        //Assert
        Assertions.assertThat(retrievedRulesBefore).isEmpty();
        Assertions.assertThat(retrievedRulesAfter.size()).isEqualTo(1);
        Assertions.assertThat(retrievedBefore.getId()).isEqualTo(retrievedAfter.getId());
        Assertions.assertThat(retrievedAddedGroup.getId()).isEqualTo(oHGroupThinDto2.getId());
    }

    @Test
    void addGroupToService() {
        //Arrange
        /*Create service*/
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        serviceDto.setId(serviceDto.getId());
        UUID serviceDtoID = serviceDto.getId();
        /*Create group*/
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getBasicGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        savedOHGroupThinDto.setId(savedOHGroupThinDto.getId());
        //Act
        openingHoursController.setOpeningHoursToService(oHGroupThinDto.getId(),serviceDto.getId());
        OHGroupDto retrievedGroup = openingHoursController.getOHGroupForService(serviceDto.getId());
        //Assert
        Assertions.assertThat(retrievedGroup.getId()).isEqualTo(savedOHGroupThinDto.getId());
    }

    @Test
    void getOpeningHoursForServiceOnDate() {
        //Arrange
        //Regler oppsett
        List<OHRuleDto> oHRulesDto = SampleDataDto.getOrderedRules();
        List<OHRuleDto>savedOHRulesDto = new ArrayList<>();
        List<UUID>savedOHRulesDtoIds = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
            savedOHRulesDtoIds.add(oHRuleDto.getId());
        });

        List<OHRuleDto>retrievedOHRulesDto = openingHoursController.getRules();

        //Group oppsett
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getRandomizedOHGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        UUID groupId = savedOHGroupThinDto.getId();
        savedOHGroupThinDto.setId(groupId);

        List<OHGroupDto>retrievedGroups = openingHoursController.getGroups();

        //add rules to group
        savedOHGroupThinDto.setRules(savedOHRulesDtoIds);
        openingHoursController.updateGroup(groupId, savedOHGroupThinDto);
        OHGroupDto retrievedGroupAfter = openingHoursController.getGroup(savedOHGroupThinDto.getId());

        //Create service
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        serviceDto.setId(serviceDto.getId());
        UUID serviceDtoID = serviceDto.getId();

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedOHGroupThinDto.getId(),serviceDto.getId());
        OHGroupDto retrievedGroupForService = openingHoursController.getOHGroupForService(serviceDto.getId());

        //Act
        String retrievedOpeningHoursSpecifiedRunDays = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "20.06.2023");
        String retrievedOpeningHoursNationalHoliday = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "17.05.2023");
        String retrievedOpeningHoursNormalWorkDays = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "21.06.2023");
        String retrievedOpeningHoursLastDayOfMonth = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "30.06.2023");
        //Assert
        Assertions.assertThat(retrievedOpeningHoursSpecifiedRunDays).isEqualTo("07:00-21:00");
        Assertions.assertThat(retrievedOpeningHoursNationalHoliday).isEqualTo("00:00-00:00");
        Assertions.assertThat(retrievedOpeningHoursNormalWorkDays).isEqualTo("07:30-17:00");
        Assertions.assertThat(retrievedOpeningHoursLastDayOfMonth).isEqualTo("07:00-18:00");
    }

    @Test
    void getOpeningHoursFromGroupForServiceOnDate() {
        //Arrange
        //Regler oppsett
        List<List<OHRuleDto>> listOfOHRulesDtos = SampleDataDto.getListOfRules ();
        List<List<UUID>>savedOHRulesDtoIds = new ArrayList<>();


        for (List<OHRuleDto> listOfOHRulesDto : listOfOHRulesDtos) {
            List<UUID> saveRulesDtoId = new ArrayList<>();
            for (OHRuleDto rulesDto : listOfOHRulesDto) {
                openingHoursController.newRule(rulesDto);
                rulesDto.setId(rulesDto.getId());
                saveRulesDtoId.add(rulesDto.getId());
            }
            savedOHRulesDtoIds.add(saveRulesDtoId);
        }

        //Group oppsett
        OHGroupThinDto basicGroupDto = SampleDataDto.getBasicGroupThinDto();
        OHGroupThinDto savedBasicGroupDto = openingHoursController.newGroup(basicGroupDto);
        savedBasicGroupDto.setId(savedBasicGroupDto.getId());
        UUID savedBasicGroupDtoId = savedBasicGroupDto.getId();

        List<OHGroupThinDto> groups =  SampleDataDto.getListOfOHGroupThinDto();
        List<OHGroupThinDto> savedGroups =  new ArrayList<>();
        List<UUID>groupsDtoIds = new ArrayList<>();
        groups.forEach(group->{
            savedGroups.add(openingHoursController.newGroup(group));
            group.setId(group.getId());
            groupsDtoIds.add(group.getId());
        });

        //add rules to group

        //EarlyClosing
        savedGroups.get(2).setRules(savedOHRulesDtoIds.get(3));
        openingHoursController.updateGroup(groups.get(2).getId(), savedGroups.get(2));

        //CollaborativeMaintenance
        savedGroups.get(1).setRules(savedOHRulesDtoIds.get(2));
        openingHoursController.updateGroup(groups.get(1).getId(),savedGroups.get(1));

        //Add Groups and Rules to LocalMaintenanceGroup

        List<UUID> maintenanceGroupRulesIds =
                Stream.of(List.of(groupsDtoIds.get(2)), //EarlyClosingIds
                                List.of(groupsDtoIds.get(1)),//CollaborativeMaintenanceIds
                                savedOHRulesDtoIds.get(1))//LocalMaintenanceRules
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //LocalMaintenance
        savedGroups.get(0).setRules(maintenanceGroupRulesIds);
        openingHoursController.updateGroup(groups.get(0).getId(),savedGroups.get(0));

        //NationalHolidays
        savedGroups.get(3).setRules(savedOHRulesDtoIds.get(4));
        openingHoursController.updateGroup(groups.get(3).getId(),savedGroups.get(3));

        List<UUID> basicGroupRulesIds =
                Stream.of(List.of(groupsDtoIds.get(3)), //BasicRulesIds
                                List.of(groupsDtoIds.get(0)),//LocalMaintenanceIds
                                savedOHRulesDtoIds.get(0))//Holidays
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        //Basic
        savedBasicGroupDto.setRules(basicGroupRulesIds);
        openingHoursController.updateGroup(savedBasicGroupDto.getId(), savedBasicGroupDto);
        OHGroupDto retrievedBasicGroupAfter = openingHoursController.getGroup(savedBasicGroupDtoId);

        //Create service
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        serviceDto.setId(serviceDto.getId());
        UUID serviceDtoID = serviceDto.getId();

        //Add group to service
        openingHoursController.setOpeningHoursToService(savedBasicGroupDtoId,serviceDto.getId());
        OHGroupDto retrievedGroupForService = openingHoursController.getOHGroupForService(serviceDto.getId());

        //Act
        String retrievedOpeningHoursChristmasDay = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "24.12.2023");
        String retrievedOpeningHoursNationalHoliday = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "17.05.2023");
        String retrievedOpeningHoursNormalWorkDays = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "21.06.2023");
        String retrievedOpeningHoursLastDayOfMonth = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "30.06.2023");
        String retrievedOpeningHoursEarlyClosingSummer = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "20.07.2023");
        String retrievedOpeningHoursLocalMaintenance1520 = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "20.09.2023");
        String retrievedOpeningHoursLocalMaintenance15 = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "02.10.2023");
        String retrievedOpeningSaturdayOutsideOfHours = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "26.08.2023");
        String retrievedOpenSaturday = openingHoursController.getOpeningHoursForServiceOnDate(serviceDto.getId(), "05.08.2023");


        // Assert
        Assertions.assertThat(retrievedOpeningHoursChristmasDay).isEqualTo("00:00-00:00");
        Assertions.assertThat(retrievedOpeningHoursNationalHoliday).isEqualTo("00:00-00:00");
        Assertions.assertThat(retrievedOpeningHoursNormalWorkDays).isEqualTo("07:30-17:00");
        Assertions.assertThat(retrievedOpeningHoursLastDayOfMonth).isEqualTo("07:00-18:00");
        Assertions.assertThat(retrievedOpeningHoursEarlyClosingSummer).isEqualTo("07:00-15:00");
        Assertions.assertThat(retrievedOpeningHoursLocalMaintenance1520).isEqualTo("07:00-16:00");
        Assertions.assertThat(retrievedOpeningHoursLocalMaintenance15).isEqualTo("07:00-16:00");
        Assertions.assertThat(retrievedOpeningSaturdayOutsideOfHours).isEqualTo("00:00-00:00");
        Assertions.assertThat(retrievedOpenSaturday).isEqualTo("10:00-15:00");

    }

    @Test
    void removeOpeningHoursFromService() {
        //Arrange
        List<OHRuleDto> oHRulesDto = SampleDataDto.getRulesDto();
        List<OHRuleDto>savedOHRulesDto = new ArrayList<>();
        List<UUID>savedOHRulesDtoIds = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
            savedOHRulesDtoIds.add(oHRuleDto.getId());
        });
        /*Create group*/
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getBasicGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        savedOHGroupThinDto.setId(savedOHGroupThinDto.getId());
        /*add rules to group*/
        oHGroupThinDto.setRules(savedOHRulesDtoIds);
        openingHoursController.updateGroup(oHGroupThinDto.getId(), oHGroupThinDto);

        /*Create service*/
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        serviceDto.setId(serviceDto.getId());

        //Set group on service
        openingHoursController.setOpeningHoursToService(oHGroupThinDto.getId(),serviceDto.getId());
        OHGroupDto retrievedGroupBefore = openingHoursController.getOHGroupForService(serviceDto.getId());
        List<OHGroupDto>retrievedRulesBeforeDto = retrievedGroupBefore.getRules();
        List<UUID>retrievedRulesBeforeDtoIds = new ArrayList<>();
        retrievedRulesBeforeDto.forEach(r->retrievedRulesBeforeDtoIds.add(r.getId()));

        //Act
        openingHoursController.removeOpeningHoursFromService(serviceDto.getId());

        //Assert
        Assertions.assertThat(retrievedGroupBefore.getId()).isEqualTo(savedOHGroupThinDto.getId());
        Assertions.assertThat(retrievedRulesBeforeDtoIds).containsAll(savedOHRulesDtoIds);
    }

    @Test
    void getOHGroupForService() {
        //Arrange
        List<OHRuleDto> oHRulesDto = SampleDataDto.getRulesDto();
        List<OHRuleDto>savedOHRulesDto = new ArrayList<>();
        List<UUID>savedOHRulesDtoIds = new ArrayList<>();
        oHRulesDto.forEach(oHRuleDto -> {
            savedOHRulesDto.add(openingHoursController.newRule(oHRuleDto));
            oHRuleDto.setId(oHRuleDto.getId());
            savedOHRulesDtoIds.add(oHRuleDto.getId());
        });
        /*Create group*/
        OHGroupThinDto oHGroupThinDto = SampleDataDto.getBasicGroupThinDto();
        OHGroupThinDto savedOHGroupThinDto = openingHoursController.newGroup(oHGroupThinDto);
        savedOHGroupThinDto.setId(savedOHGroupThinDto.getId());
        /*add rules to group*/
        oHGroupThinDto.setRules(savedOHRulesDtoIds);
        openingHoursController.updateGroup(oHGroupThinDto.getId(), oHGroupThinDto);

        /*Create service*/
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        ServiceDto serviceDto = serviceController.newService(EntityDtoMappers.toServiceDtoShallow(service));
        serviceDto.setId(serviceDto.getId());

        //Set group on service
        openingHoursController.setOpeningHoursToService(oHGroupThinDto.getId(),serviceDto.getId());
        //Act
        OHGroupDto retrievedGroupDto = openingHoursController.getOHGroupForService(serviceDto.getId());

        //Act
        List<OHGroupDto>retrievedRulesDto = retrievedGroupDto.getRules();
        List<UUID>retrievedRulesDtoIds = new ArrayList<>();
        retrievedRulesDto.forEach(r->retrievedRulesDtoIds.add(r.getId()));

        //Assert
        Assertions.assertThat(retrievedGroupDto.getId()).isEqualTo(savedOHGroupThinDto.getId());
        Assertions.assertThat(retrievedRulesDtoIds).containsAll(savedOHRulesDtoIds);

    }

}
