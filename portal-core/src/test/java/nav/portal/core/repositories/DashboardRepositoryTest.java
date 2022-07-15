package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.AreaWithServices;
import nav.portal.core.entities.DashboardEntity;
import nav.portal.core.entities.ServiceEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.*;
import java.util.stream.Collectors;

class DashboardRepositoryTest {

    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();
    private final DashboardRepository dashboardRepository = new DashboardRepository(dbContext);
    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);
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


    @Test
    void save() {
        //Arrange
        String dashboardName = SampleData.getRandomizedDashboardName();
        //Act
        UUID uuid = dashboardRepository.save(dashboardName);
        //Assert
        Assertions.assertThat(uuid).isNotNull();
    }



    @Test
    void settAreasOnDashboard() {
        //Arrange
        //Sett opp et dashboard java-obj
        String dashboardName =SampleData.getRandomizedDashboardName();
        //Lagra java-obj ned i db
        UUID dashboard_id = dashboardRepository.save(dashboardName);
        //Sett opp en liste av områder(area)
        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();
        //Lagrer alle ned i db

        List<UUID> areas_ids = new ArrayList<>();
        areas.forEach(a ->{ a.setId(areaRepository.save(a));
            areas_ids.add(a.getId());});

        //Act
        //Knytter områdene til dashboard
        dashboardRepository.settAreasOnDashboard(dashboard_id,areas_ids);
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
    void updateNameOfDashboard(){
        //Arrange
        String dashboardName =SampleData.getRandomizedDashboardName();
        //Lagra java-obj ned i db
        UUID dashboardId = dashboardRepository.save(dashboardName);
        //Act
        String newDashboardName = "Non existing name";
        dashboardRepository.updateNameOfDashboard(dashboardId, newDashboardName);
        Map.Entry<DashboardEntity, List<AreaWithServices>>retrievedDashboardEntity = dashboardRepository.retrieveOne(dashboardId);
        //Assert
        Assertions.assertThat(retrievedDashboardEntity.getKey().getName()).isNotEqualToIgnoringCase(dashboardName);
        Assertions.assertThat(retrievedDashboardEntity.getKey().getName()).isEqualTo(newDashboardName);
    }

    @Test
    void getAllDashboardUUIDsAndNames() {
        //Arrange
        List<String> dashboardNames = SampleData.getDashboardNames();
        dashboardNames.forEach(dashboardRepository::save);

        //Act
        List<DashboardEntity> retrievedDashboards = dashboardRepository.getAllDashboardUUIDsAndNames();
        //Assert
        Map<String, DashboardEntity> comparisons = new HashMap<>();

        /* Under skrevet med forEach
        retrievedDashboards.forEach(retrievedDashboard ->
                comparisons.put(dashboardNames.stream().filter(name -> retrievedDashboard.getName().equals(name))
                                .findFirst().orElseThrow(),
                     retrievedDashboard));
        */
        // Under skrevet med vanlig for loop
        for (DashboardEntity dashboardEntity : retrievedDashboards) {
            String dashboardName = dashboardNames.stream().filter(name -> dashboardEntity.getName().equals(name)).findFirst().orElseThrow();
            comparisons.put(dashboardName, dashboardEntity);
        }

        comparisons.forEach((name, entity) -> {
            Assertions.assertThat(name).isEqualTo(entity.getName());
            Assertions.assertThat(entity.getId()).isExactlyInstanceOf(UUID.class);
        });
    }

    @Test
    void uidFromName(){
    //Arrange
    String name = SampleData.getRandomizedDashboardName();
    dashboardRepository.save(name);
    //Act
    UUID uuid = dashboardRepository.uidFromName(name);
    //Assert
    Assertions.assertThat(uuid).isExactlyInstanceOf(UUID.class);
    }

    @Test
    void retrieveOne() {
        //TODO denne
        //Arrange -
        String dashboardName = SampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        List<ServiceEntity>services = SampleData.getRandomLengthNonEmptyListOfServiceEntity();
        List<UUID>serviceIds = new ArrayList<>();
        services.forEach(service -> {service.setId(serviceRepository.save(service));
                                     serviceIds.add(service.getId());}
        );
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        List<UUID>areaIds = new ArrayList<>();
        area.setId(areaRepository.save(area));
        areaIds.add(area.getId());
        areaRepository.setServicesOnArea(area.getId(), serviceIds);
        dashboardRepository.settAreasOnDashboard(dashboardId, areaIds);
        //Act
        Map.Entry<DashboardEntity, List<AreaWithServices>>exists = dashboardRepository.retrieveOne(dashboardId);
        //Assert
        Assertions.assertThat(exists.getKey().getName()).isEqualTo(dashboardName);
        Assertions.assertThat(exists.getValue().get(0).getArea()).isEqualTo(area);
        Assertions.assertThat(exists.getValue().get(0).getServices()).containsAll(services);
    }

    @Test
    void retrieveOneFromName() {
        //TODO denne
        //Arrange -
        String dashboardname = SampleData.getRandomizedDashboardName();
        UUID uuid = dashboardRepository.save(dashboardname);
        //Act
        Map.Entry<DashboardEntity, List<AreaWithServices>> retrievedDashboard = dashboardRepository.retrieveOneFromName(dashboardname);
        //Assert
        Assertions.assertThat(retrievedDashboard.getKey().getName()).isEqualTo(dashboardname);
        Assertions.assertThat(uuid).isEqualTo(retrievedDashboard.getKey().getId());
    }

    @Test
    void retrieveAll() {
        //Arrange
        String dashboardName = SampleData.getRandomizedDashboardName();
        UUID dashboard_id = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();
        List<UUID> areas_ids =  areas.stream()
                .map(areaRepository::save)
                .collect(Collectors.toList());
        dashboardRepository.settAreasOnDashboard(dashboard_id,areas_ids);
        //Act;
        Map<DashboardEntity, List<AreaWithServices>> retrievedAll = dashboardRepository.retrieveAll();

        Map.Entry<DashboardEntity, List<AreaWithServices>> dashboardWithAreas = retrievedAll.entrySet()
                .stream()
                .findFirst()
                .orElseThrow();

        //Assert
        //Sjekker at id på dashboard er riktig
        Assertions.assertThat(dashboardWithAreas.getKey().getId()).isEqualTo(dashboard_id);

        //Sjekker at dashboard navnet er riktig
        Assertions.assertThat(dashboardWithAreas.getKey().getName()).isEqualTo(dashboardName);

        //Sjekke at områdene er blitt lagt til riktig
        /*List<AreaEntity> retrievedAreas =  dashboardWithAreas.getValue()
                .stream()
                .map(AreaWithServices::getArea)
                .collect(Collectors.toList());
        Assertions.assertThat(retrievedAreas).isEqualTo(areas);


        //Sjekker at ingen av områdene har tjenester knyttet til seg
        List<List<ServiceEntity>> servicesOnAreas =  dashboardWithAreas.getValue()
                .stream()
                .map(AreaWithServices::getServices)
                .collect(Collectors.toList());

        servicesOnAreas.forEach(list -> Assertions.assertThat(list).isEmpty());*/
    }


    @Test
    void deleteAreasFromDashboard() {
        //Arrange
        String dashboardname = SampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardname);

        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();

        List<UUID> areaIds = new ArrayList<>();
        areas.forEach(area -> {area.setId(areaRepository.save(area));
                               areaIds.add(area.getId());});
        dashboardRepository.settAreasOnDashboard(dashboardId, areaIds);

        //Act
        dashboardRepository.deleteAreasFromDashboard(dashboardId);
        Map.Entry<DashboardEntity, List<AreaWithServices>> result = dashboardRepository.retrieveOneFromName(dashboardname);
        //Assert
        Assertions.assertThat(result.getValue()).isEmpty();
    }


    @Test
    void deleteDashboard() {
        //Arrange
        String dashboardname = SampleData.getRandomizedDashboardName();
        UUID uuid = dashboardRepository.save(dashboardname);
        //Act
        UUID shouldExist = dashboardRepository.uidFromName(dashboardname);
        dashboardRepository.deleteDashboard(uuid);
        //Assert
        Assertions.assertThat(shouldExist).isEqualTo(uuid);
        Assertions.assertThat(dashboardRepository.getAllDashboardUUIDsAndNames()).isEmpty();
    }
}