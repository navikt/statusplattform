package no.nav.portal.rest.api.v3.controllers;
import nav.portal.core.entities.*;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.DashboardDto;
import no.portal.web.generated.api.DashboardNameIdDto;

import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

class DashboardControllerTest {
    private final SampleData sampleData = new SampleData();
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;


    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final DashboardController dashboardController = new DashboardController(dbContext);
    private final DashboardRepository dashboardRepository = new DashboardRepository(dbContext);

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
    }

    @AfterEach
    void endConnection() {
        TestUtil.clearAllTableData(dbContext);
        connection.close();
    }

    @Test
    void getDashboards() {
        //Arrange
        List<String> dashboardNames = sampleData.getDashboardNames();
        dashboardNames.forEach(dashboardRepository::save);
        //Act
        List<DashboardNameIdDto> dashboardNameIdDto = dashboardController.getDashboards();
        //Assert
        List<String> retrievedNames = new ArrayList<>();
        /*for(DashboardNameIdDto d: dashboardNameIdDto) {
            retrievedNames.add(d.getName());
        }*/
        //for-each loop og lambda
        dashboardNameIdDto.forEach(d -> retrievedNames.add(d.getName()));
        Assertions.assertThat(retrievedNames).containsAll(dashboardNames);
    }

    @Test
    void postDashboard() {
        String dashboardName = sampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = sampleData.getNonEmptyListOfAreaEntity(3);
        List<UUID> areaIds = areas.stream().map(areaRepository::save).collect(Collectors.toList());
        dashboardRepository.settAreasOnDashboard(dashboardId,areaIds);
        DashboardDto dashboardDto = dashboardController.getAreas(dashboardId);
        //Act
        UUID afterId = dashboardController.postDashboard(dashboardDto);
        //Assert
        Map.Entry<DashboardEntity, List<AreaWithServices>>posted = dashboardRepository.retrieveOne(afterId);
        Assertions.assertThat(posted.getKey().getName()).isEqualTo(dashboardName);
        Assertions.assertThat(posted.getValue().size()).isEqualTo(areaIds.size());
    }

    @Test
    void deleteDashboard() {
        //Arrange
        String dashboardname = sampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardname);
        UUID shouldExist = dashboardRepository.uidFromName(dashboardname);
        List<AreaEntity> areas = sampleData.getRandomLengthListOfAreaEntity();
        List<UUID> areaIds = areas.stream()
                .map(areaRepository::save)
                .collect(Collectors.toList());
        dashboardRepository.settAreasOnDashboard(dashboardId, areaIds);
        //Act
        dashboardController.deleteDashboard(dashboardId);
        //Assert
        Assertions.assertThat(shouldExist).isEqualTo(dashboardId);
        Assertions.assertThat(dashboardRepository.getAllDashboardUUIDsAndNames()).isEmpty();
    }

    @Test
    void addAreaToDashboard() {
        //Arrange
        String dashboardName = sampleData.getRandomizedDashboardName();
        UUID dashboard_id = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = sampleData.getRandomLengthListOfAreaEntity();
        List<UUID> areas_ids = new ArrayList<>();
        for(AreaEntity area: areas){
            UUID id = areaRepository.save(area);
            areas_ids.add(id);
            area.setId(id);
        }
        //Act
        dashboardController.addAreaToDashboard(dashboard_id,areas_ids);
        Map.Entry<DashboardEntity,List<AreaWithServices>> dashboardWithAreas = dashboardRepository.retrieveOne(dashboard_id);
        //Assert
        //Sjekker at id på dashboard er riktig
        Assertions.assertThat(dashboardWithAreas.getKey().getId()).isEqualTo(dashboard_id);
        //Sjekker at dashboard navnet er riktig
        Assertions.assertThat(dashboardWithAreas.getKey().getName()).isEqualTo(dashboardName);
        //Sjekke at områdene er blitt lagt til riktig
        List<AreaEntity> retrievedAreas =  dashboardWithAreas.getValue()
                .stream()
                .map(AreaWithServices::getArea)
                .collect(Collectors.toList());
        Assertions.assertThat(retrievedAreas).isEqualTo(areas);
       //Sjekker at ingen av områdene har tjenester knyttet til seg
        List<List<ServiceEntity>> servicesOnAreas =  dashboardWithAreas.getValue()
                .stream()
                .map(AreaWithServices::getServices)
                .collect(Collectors.toList());

        servicesOnAreas.forEach(list -> Assertions.assertThat(list).isEmpty());
    }

    @Test
    void updateNameOfDashboard() {
        //Arrange
        String dashboardName = sampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = sampleData.getNonEmptyListOfAreaEntity(3);
        List<UUID> areaIds = areas.stream().map(areaRepository::save).collect(Collectors.toList());
        dashboardRepository.settAreasOnDashboard(dashboardId,areaIds);
        DashboardDto dashboardDto = dashboardController.getAreas(dashboardId);
        String oldName = dashboardDto.getName();
        String newName = "Test";
        dashboardDto.setName(newName);
        //Act
        dashboardController.updateNameOfDashboard(dashboardId, dashboardDto);
        Map.Entry<DashboardEntity, List<AreaWithServices>>aName = dashboardRepository.retrieveOneFromName(newName);
        String retrievedName = aName.getKey().getName();
        //Assert
        Assertions.assertThat(retrievedName).isEqualTo(newName);
        //Assertions.assertThat(retrievedName).isNotEqualTo(oldName);

    }

    @Test
    void getAreas() {
        //Arrange
        String dashboardName = sampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = sampleData.getNonEmptyListOfAreaEntity(3);
        List<UUID> areaIds = areas.stream().map(areaRepository::save).collect(Collectors.toList());
        dashboardRepository.settAreasOnDashboard(dashboardId,areaIds);
        //Act
        DashboardDto dashboardDto = dashboardController.getAreas(dashboardId);
        UUID afterId = dashboardDto.getId();
        //Assert
        Assertions.assertThat(afterId).isEqualTo(dashboardId);

    }
}