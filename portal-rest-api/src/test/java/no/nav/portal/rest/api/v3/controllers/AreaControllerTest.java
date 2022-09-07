package no.nav.portal.rest.api.v3.controllers;


import nav.portal.core.entities.ServiceEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import nav.portal.core.entities.AreaEntity;
import no.portal.web.generated.api.AreaDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

class AreaControllerTest {

    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final AreaController areaController = new AreaController(dbContext);
    private final AreaRepository areaRepository = new AreaRepository(dbContext);
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
    void getAllAreas() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        area.setId(areaId);

        List<ServiceEntity> services = SampleData.getNonEmptyListOfServiceEntity(3);
        /*for(ServiceEntity service : services){
            service.setId(serviceRepository.save(service));
            areaRepository.addServiceToArea(areaId, service.getId());
        }*/
        services.forEach(s ->
        {
            s.setId(serviceRepository.save(s));
            areaRepository.addServiceToArea(areaId, s.getId());
        });

        //Act
        List<AreaDto> retrievedArea = areaController.getAllAreas();
        //Assert
        Assertions.assertThat(retrievedArea.get(0).getId()).isEqualTo(areaId);
    }

    @Test
    void newArea() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        AreaDto areaDto = EntityDtoMappers.toAreaDtoShallow(area);
        //Act
        //UUID areaId = areaController.newArea(areaDto);
        //area.setId(areaId);
        //AreaEntity retrievedEntity = areaRepository.retrieveOne(areaId).getKey();
        //Assert
        //Assertions.assertThat(retrievedEntity).isEqualTo(area);
    }

    @Test
    void updateArea() {
        //Arrange
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(2);
        List<AreaDto> areaDtos = EntityDtoMappers.toAreaDtoShallow(areas);

        UUID areaId1 = areaRepository.save(areas.get(0));

        areas.forEach(area -> area.setId(areaId1));

        AreaEntity before = areaRepository.retrieveOne(areaId1).getKey();
        //Act
        areaController.updateArea(areaId1, areaDtos.get(1));
        //Assert
        AreaEntity after = areaRepository.retrieveOne(areaId1).getKey();
        Assertions.assertThat(after.getId()).isEqualTo(before.getId());
        Assertions.assertThat(after).isNotEqualTo(before);
    }

    @Test
    void deleteArea() {
        //Arrange
        int NumberOfAreas = 1;
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(NumberOfAreas);
        /*for(AreaEntity area :areas){
            area.setId(areaRepository.save(area));
        }*/

        // Alternativ 2 med bruk av for-each loop og lambda
        areas.forEach(area -> area.setId(areaRepository.save(area)));
//
//        // Alternativ 3 med bruk av stream og lambda
//        List<UUID> areas_ids =  areas.stream()
//                .map(areaRepository::save)
//               .collect(Collectors.toList());

        AreaEntity areaToBeDeleted = areas.get(0);
        //Act
        List<AreaEntity> retrievedBeforeDelete = areaRepository.retriveAllShallow();
        areaController.deleteArea(areaToBeDeleted.getId());
        List<AreaEntity> retrievedAreasAfterDelete = areaRepository.retriveAllShallow();
        retrievedBeforeDelete.removeAll(retrievedAreasAfterDelete);

        //Assert
        /*Assertions.assertThat(areas.isEmpty());
        Assertions.assertThat(retrievedBeforeDelete.size()).isEqualTo(1);
        Assertions.assertThat(retrievedBeforeDelete.get(0)).isEqualTo(areaToBeDeleted);*/
    }


    @Test
    void getAreas() {
        //Arrange
        String dashboardName = SampleData.getRandomizedDashboardName();
        UUID dashboardId = dashboardRepository.save(dashboardName);
        List<AreaEntity> areas = SampleData.getNonEmptyListOfAreaEntity(3);
        List<UUID> areaIds = areas.stream().map(areaRepository::save).collect(Collectors.toList());
        List<AreaDto> beforeDto = EntityDtoMappers.toAreaDtoDeep(areaRepository.retrieveAll());
        dashboardRepository.settAreasOnDashboard(dashboardId,areaIds);
        //Act
        List<AreaDto> afterDto = areaController.getAreas(dashboardId);
        //Assert
        Assertions.assertThat(afterDto.size()).isEqualTo(3);
        Assertions.assertThat(afterDto.size()).isEqualTo(beforeDto.size());
        Assertions.assertThat(afterDto.containsAll(beforeDto)).isTrue();
    }


    @Test
    void addServiceToArea() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);

        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        //Act
        areaController.addServiceToArea(areaId, serviceId );
        Map.Entry<AreaEntity,List<ServiceEntity>> retrievedArea = areaRepository.retrieveOne(areaId);
        //Assert
        Assertions.assertThat(retrievedArea.getValue()).containsExactly(service);
    }

    @Test
    void removeServiceFromArea() {
        //Arrange
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);

        ServiceEntity service = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(service);
        service.setId(serviceId);
        areaController.addServiceToArea(areaId, serviceId);
        Map.Entry<AreaEntity, List<ServiceEntity>> before = areaRepository.retrieveOne(areaId);
        //Act
        areaRepository.removeServiceFromArea(areaId, serviceId);
        Map.Entry<AreaEntity, List<ServiceEntity>> after = areaRepository.retrieveOne(areaId);
        //Assert
        Assertions.assertThat(before.getValue()).containsExactly(service);
        Assertions.assertThat(after.getValue()).isEmpty();
    }
}