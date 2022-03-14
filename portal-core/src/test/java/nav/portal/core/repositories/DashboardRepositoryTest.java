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

    private DataSource dataSource = TestDataSource.create();

    private SampleData sampleData = new SampleData();


    private DbContext dbContext = new DbContext();
    private DbContextConnection connection;

    @BeforeEach
    void startConnection() {
        connection = dbContext.startConnection(dataSource);
    }

    @AfterEach
    void endConnection() {
        TestUtil.clearAllTableData(dbContext);
        connection.close();
    }

    private final DashboardRepository dashboardRepository = new DashboardRepository(dbContext);
    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);

    @Test
    void save() {
        //Arrange
        String dashboardName = sampleData.getRandomizedDashboardName();
        //Act
        UUID uuid = dashboardRepository.save(dashboardName);
        //Assert
        Assertions.assertThat(uuid).isNotNull();
    }


    /*
    @Test
    void settAreasOnDashboard() {
        //Arrange
        //Sett opp et dashboard java-obj
        String dashboardName = sampleData.getRandomizedDashboardName();
        //Lagra java-obj ned i db
        UUID dashboard_id = dashboardRepository.save(dashboardName);
        //Sett opp en liste av områder(area)
        List<AreaEntity> areas = sampleData.getRandomLengthListOfAreaEntity();
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

    }*/

    @Test
    void getAllDashboardUUIDsAndNames() {
        //Arrange
        List<String> dashboardNames = sampleData.getDashboardNames();
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
        for(int i = 0; i < retrievedDashboards.size(); i++){
            DashboardEntity dashboardEntity = retrievedDashboards.get(i);
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
    String name = sampleData.getRandomizedDashboardName();
    dashboardRepository.save(name);
    //Act
    UUID uuid = dashboardRepository.uidFromName(name);
    //Assert
    Assertions.assertThat(uuid).isExactlyInstanceOf(UUID.class);
    }

    /*
    @Test
    void retrieveOne() {
        //TODO denne
        //Arrange -
        String dashboardname = "Dashboard";
        UUID dashboardId = dashboardRepository.save(dashboardname);

        List<AreaEntity> areas = SampleData.getRandomLengthListOfAreaEntity();

        List<UUID> areaIds =  new ArrayList<>();
        areas.forEach(area -> {area.setId(areaRepository.save(area));
                               areaIds.add(area.getId());}
        );

        dashboardRepository.settAreasOnDashboard(dashboardId,areaIds);
        //Act
        Map.Entry<DashboardEntity, List<AreaWithServices>>exists = dashboardRepository.retrieveOne(dashboardId);
        //Assert
        Assertions.assertThat(exists.getKey().getName()).isEqualTo(dashboardname);
        Assertions.assertThat(exists.getValue().size()).isEqualTo(areaIds.size());
    }
*/

    @Test
    void retrieveOneFromName() {
        //TODO denne
        //Arrange -
        String dashboardname = sampleData.getRandomizedDashboardName();
        UUID uuid = dashboardRepository.save(dashboardname);
        //Act
        Map.Entry<DashboardEntity, List<AreaWithServices>>aName = dashboardRepository.retrieveOneFromName(dashboardname);
        //Assert
        Assertions.assertThat(aName.getKey().getName()).isEqualTo(dashboardname);

    }

    @Test
    void retrieveAll() {
        //Arrange
        String dashboardName = sampleData.getRandomizedDashboardName();
        UUID dashboard_id = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = sampleData.getRandomLengthListOfAreaEntity();
        List<UUID> areas_ids =  areas.stream()
                .map(areaRepository::save)
                .collect(Collectors.toList());
        dashboardRepository.settAreasOnDashboard(dashboard_id,areas_ids);
        //Act;
        Map<DashboardEntity, List<AreaWithServices>> retrievedAll = dashboardRepository.retrieveAll();

        Map.Entry<DashboardEntity, List<AreaWithServices>> dashboardWithAreas = retrievedAll.entrySet()
                .stream()
                .findFirst()
                .get();

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
        String dashboardname = sampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardname);

        List<AreaEntity> areas = sampleData.getRandomLengthListOfAreaEntity();
        /*List<UUID> areaIds = areas.stream()
                .map(areaRepository::save)
                .collect(Collectors.toList());*/

        List<UUID> areaIds = new ArrayList<>();
        areas.forEach(area -> {area.setId(areaRepository.save(area));
                               areaIds.add(area.getId());});
        dashboardRepository.settAreasOnDashboard(dashboardId, areaIds);

        //Act
        dashboardRepository.deleteAreasFromDashboard(dashboardId);
        Map.Entry<DashboardEntity, List<AreaWithServices>> result = dashboardRepository.retrieveOneFromName(dashboardname);
        //Assert

        Assertions.assertThat(result.getValue().size()).isEqualTo(0);
    }
    @Test
    void deleteDashboard() {
        //Arrange
        String dashboardname = sampleData.getRandomizedDashboardName();
        UUID uuid = dashboardRepository.save(dashboardname);
        //Act
        UUID shouldExist = dashboardRepository.uidFromName(dashboardname);
        dashboardRepository.deleteDashboard(uuid);
        //Assert
        Assertions.assertThat(shouldExist).isEqualTo(uuid);
        Assertions.assertThat(dashboardRepository.getAllDashboardUUIDsAndNames()).isEmpty();
    }
}