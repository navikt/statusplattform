package no.nav.portal.rest.api.v3.controllers;
import nav.portal.core.entities.*;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.portal.web.generated.api.DashboardDto;
import no.portal.web.generated.api.DashboardNameIdDto;

import no.portal.web.generated.api.DashboardUpdateDto;
import no.portal.web.generated.api.IdContainerDto;
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
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final DashboardController dashboardController = new DashboardController(dbContext);
    private final DashboardRepository dashboardRepository = new DashboardRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

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
    void getDashboards() {
        //Arrange
        List<String> dashboardNames = SampleData.getDashboardNames();
        dashboardNames.forEach(dashboardRepository::save);
        //Act
        List<DashboardNameIdDto> dashboardNameIdDto = dashboardController.getDashboards();
        //Assert
        List<String> retrievedNamesFromStream = dashboardNameIdDto.stream()
                .map(DashboardNameIdDto::getName)
                .collect(Collectors.toList());
        /*for(DashboardNameIdDto d: dashboardNameIdDto) {
            retrievedNames.add(d.getName());
        }*/
        //for-each loop og lambda
        //dashboardNameIdDto.forEach(d -> retrievedNames.add(d.getName()));
        Assertions.assertThat(retrievedNamesFromStream).containsExactlyInAnyOrderElementsOf(dashboardNames);
    }


    @Test
    void postDashboard() {
        String dashboardName = SampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        areaRepository. addServiceToArea(areaId, serviceId);
        dashboardRepository.settAreasOnDashboard(dashboardId,areaId);
        Map.Entry<DashboardEntity, List<AreaWithServices>>dashboardWithAreas = dashboardRepository.retrieveOne(dashboardId);
        DashboardDto dashboardDto = EntityDtoMappers.toDashboardDtoDeep(dashboardWithAreas);
        //Act
        IdContainerDto idContainerDto = dashboardController.postDashboard(dashboardDto);
        //Assert
        Map.Entry<DashboardEntity, List<AreaWithServices>>posted = dashboardRepository.retrieveOne(idContainerDto.getId());
        Assertions.assertThat(posted.getKey().getName()).isEqualTo(dashboardName);
        Assertions.assertThat(posted.getValue()).isNotEmpty();
        Assertions.assertThat(posted.getValue().containsAll(dashboardWithAreas.getValue())).isTrue();
    }

    @Test
    void deleteDashboard() {
        //Arrange
        String dashboardname = SampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardname);
        UUID shouldExist = dashboardRepository.uidFromName(dashboardname);
        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();
        List<UUID> areaIds = areas.stream()
                .map(areaRepository::save)
                .collect(Collectors.toList());
        /*List<UUID> areaIds = new ArrayList<>();
        areas.forEach(area -> {area.setId(areaRepository.save(area));
            areaIds.add(area.getId());
        });*/
        dashboardRepository.settAreasOnDashboard(dashboardId, areaIds);
        //Act
        dashboardController.deleteDashboard(dashboardId);
        //Assert
        Assertions.assertThat(shouldExist).isEqualTo(dashboardId);
        Assertions.assertThat(dashboardRepository.getAllDashboardUUIDsAndNames()).isEmpty();
    }

/*
    @Test
    void addAreaToDashboard() {
        //Arrange
        String dashboardName = SampleData.getRandomizedDashboardName();
        UUID dashboard_id = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();
        List<UUID> areas_ids = new ArrayList<>();


        areas.forEach(area ->
        {area.setId(areaRepository.save(area));
            areas_ids.add(area.getId());
        });


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

 */

    @Test
    void updateNameOfDashboard() {
        //Arrange
        String dashboardName = SampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(3);
        /*List<UUID> areaIds = areas.stream().map(areaRepository::save).collect(Collectors.toList());*/
        List<UUID> areaIds = new ArrayList<>();
        areas.forEach(area ->
            {area.setId(areaRepository.save(area));
            areaIds.add(area.getId());
            });

        dashboardRepository.settAreasOnDashboard(dashboardId,areaIds);
        DashboardDto dashboardDto = dashboardController.getDashboard(dashboardId);

        String oldName = dashboardDto.getName();
        String newName = "Test";
        dashboardDto.setName(newName);
        DashboardUpdateDto dashboardUpdateDto = new DashboardUpdateDto().name(newName).areas(areaIds);
        //Act
        dashboardController.updateDashboard(dashboardId, dashboardUpdateDto);
        Map.Entry<DashboardEntity, List<AreaWithServices>>aName = dashboardRepository.retrieveOneFromName(newName);
        String retrievedName = aName.getKey().getName();
        //Assert
        Assertions.assertThat(retrievedName).isEqualTo(newName);
        Assertions.assertThat(retrievedName).isNotEqualTo(oldName);

    }

    @Test
    void getDashboard() {
        //Arrange
        String dashboardName = SampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(3);
        /*List<UUID> areaIds = areas.stream().map(areaRepository::save).collect(Collectors.toList());*/
        List<UUID> areaIds = new ArrayList<>();
        areas.forEach(area -> {area.setId(areaRepository.save(area));
                     areaIds.add(area.getId());
         });
        dashboardRepository.settAreasOnDashboard(dashboardId,areaIds);
        //Act
        DashboardDto dashboardDto = dashboardController.getDashboard(dashboardId);
        UUID afterId = dashboardDto.getId();
        //Assert
        Assertions.assertThat(afterId).isEqualTo(dashboardId);

    }
}