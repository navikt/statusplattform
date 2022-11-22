package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.OpsMessageEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.OpsControllerHelper;
import no.portal.web.generated.api.*;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.units.qual.A;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

class OpsControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private final DashboardController dashboardController = new DashboardController(dbContext);
    private final AreaController areaController = new AreaController(dbContext);
    private final ServiceController serviceController = new ServiceController(dbContext);
    private final OpsController opsController = new OpsController(dbContext);
    private final OpsControllerHelper opsControllerHelper = new OpsControllerHelper(dbContext);
    private final DashboardRepository dashboardRepository = new DashboardRepository(dbContext);
    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final OpsRepository opsRepository = new OpsRepository(dbContext);
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
    void createOpsMessage() {
        //Arrange
        AreaDto areaDto = SampleDataDto.getRandomizedAreaDto();
        IdContainerDto idContainerDto = areaController.newArea(areaDto);
        areaDto.setId(idContainerDto.getId());

        /*ServiceDto serviceDto = SampleDataDto.getRandomizedServiceDto();
        ServiceDto savedServiceDto = serviceController.newService(serviceDto);
        serviceDto.setId(savedServiceDto.getId());*/

        List<ServiceDto> serviceDtos = SampleDataDto.getRandomLengthListOfServiceDto();
        serviceDtos.forEach(serviceDto1 -> {
           ServiceDto savedServiceDto1 = serviceController.newService(serviceDto1);
           serviceDto1.setId(savedServiceDto1.getId());
        });

        DashboardDto dashboardDto = SampleDataDto.getRandomizedDashboardDto();
        dashboardDto.setAreas(List.of(areaDto));
        IdContainerDto dashboardIdContainerDto = dashboardController.postDashboard(dashboardDto);
        dashboardDto.setId(dashboardIdContainerDto.getId());

        OPSmessageDto opsMessageDto = SampleDataDto.getRandomOPSMessageDto();
        opsMessageDto.setAffectedServices(serviceDtos);
        ///opsMessageDto.setAffectedServices(List.of(serviceDto));
        //Act
        OPSmessageDto createdOpsMessage = opsController.createOpsMessage(opsMessageDto);
        OPSmessageDto retrievedOpsMessage = opsController.getSpecificOpsMessage(createdOpsMessage.getId());
        List<ServiceDto> affectedServices = opsMessageDto.getAffectedServices();

        //Assert
        Assertions.assertThat(retrievedOpsMessage).isEqualTo(createdOpsMessage);
        Assertions.assertThat(createdOpsMessage.getInternalHeader()).isEqualTo(opsMessageDto.getInternalHeader());
        Assertions.assertThat(createdOpsMessage.getInternalMessage()).isEqualTo(opsMessageDto.getInternalMessage());
        Assertions.assertThat(affectedServices).containsExactlyInAnyOrderElementsOf(serviceDtos);
        Assertions.assertThat(affectedServices.size()).isEqualTo(serviceDtos.size());
        //Assertions.assertThat(affectedServices).contains(serviceDto);
      }

    @Test
    void getAllForDashboard() {
        //Lager dashboard:
        UUID dashbaordId = dashboardRepository.save("Mitt nye fantastiske dashboard!");

        //Lager område og legger til på dashboard:
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        dashboardRepository.settAreasOnDashboard(dashbaordId,areaId);

        //Lager tjeneste og legger den på område:
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(serviceEntity);
        serviceEntity.setId(serviceId);
        areaRepository.addServiceToArea(areaId,serviceId);


        //Lager ops Message som IKKE er knyttet til noen tjeneste:
        OpsMessageEntity opsMessageEntity = SampleData.getRandomOpsMessageEntity();
        opsMessageEntity.setId(opsRepository.save(opsMessageEntity, new ArrayList<>()));
        //Mapper til dto, og setter på tjeneste
        OPSmessageDto opSmessageDto = EntityDtoMappers.toOpsMessageDtoShallow(opsMessageEntity);
        opSmessageDto.setAffectedServices(List.of(EntityDtoMappers.toServiceDtoShallow(serviceEntity)));

        //------------------ Act ------------------------------------

        //Henter først alle opsmeldinger på dashboardet. Nå skal ingen meldinger ligge på dashboardet.
        //EDIT: Endret funksjonalitet: Opsmeldinger som ikke er knyttet til noe dashboard, skal komme opp på alle,
        List<OPSmessageDto> shouldAlsoContainOne = opsController.getAllForDashboard(dashbaordId);
        //Kobler opsmeldingen til tjenesten
        opsRepository.setServicesOnOpsMessage(opsMessageEntity.getId(), List.of(serviceEntity.getId()));
        //Nå skal opsmeldingen være koblet mot dashboardet via tjenesten:
        List<OPSmessageDto> shouldContainOne = opsController.getAllForDashboard(dashbaordId);



        //------------------ Assert ------------------------------------
        Assertions.assertThat(shouldAlsoContainOne.size()).isEqualTo(1);
        Assertions.assertThat(shouldContainOne.size()).isEqualTo(1);
        Assertions.assertThat(shouldContainOne.get(0).getId()).isEqualTo(opSmessageDto.getId());

    }

    //@Test
    //TODO FIX
    void getAllOpsMessages() {
        //Arrange
        List<OpsMessageEntity> opsMessagesEntitiesList = SampleData.getNonEmptyListOfOpsMessageEntity(1);
        List<OPSmessageDto> opsMessagesDtoList = opsMessagesEntitiesList
                .stream().map(EntityDtoMappers::toOpsMessageDtoShallow)
                .collect(Collectors.toList());

        //Act
        opsMessagesDtoList.forEach(dto -> dto.setId(opsController.createOpsMessage(dto).getId()));
        List<OPSmessageDto> retrievedOpsMessages = opsController.getAllOpsMessages();

        //Assert
        Assertions.assertThat(opsMessagesDtoList).containsExactlyInAnyOrderElementsOf(retrievedOpsMessages);
    }

    @Test
    void deleteOpsMessage() {
        //Arrange
        OpsMessageEntity opsMessageEntity = SampleData.getRandomOpsMessageEntity();
        OPSmessageDto opsMessageDto = EntityDtoMappers.toOpsMessageDtoShallow(opsMessageEntity);

        //Act
        UUID opsMessageId = opsController.createOpsMessage(opsMessageDto).getId();
        opsMessageEntity.setId(opsMessageId);
        OpsMessageEntity retrievedEntity = opsRepository.retrieveOne(opsMessageId).getKey();
        
        Boolean isEntryDeleted = opsRepository.isEntryDeleted(retrievedEntity.getId());
        opsController.deleteOpsMessage(retrievedEntity.getId());

        //Assert
        Assertions.assertThat(isEntryDeleted).isEqualTo(false);
        Assertions.assertThat(opsRepository.isEntryDeleted(opsMessageId)).isEqualTo(true);
    }

    @Test
    void getSpecificOpsMessage(){
        //Arrange
        UUID dashbaordId = dashboardRepository.save("A Dashboard");

        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);
        dashboardRepository.settAreasOnDashboard(dashbaordId,areaId);

        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(serviceEntity);
        serviceEntity.setId(serviceId);
        areaRepository.addServiceToArea(areaId,serviceId);

        OpsMessageEntity opsMessageEntity = SampleData.getRandomOpsMessageEntity();
        opsMessageEntity.setId(opsRepository.save(opsMessageEntity, new ArrayList<>()));
        OPSmessageDto opSmessageDto = EntityDtoMappers.toOpsMessageDtoShallow(opsMessageEntity);
        opSmessageDto.setAffectedServices(List.of(EntityDtoMappers.toServiceDtoShallow(serviceEntity)));
        opsRepository.setServicesOnOpsMessage(opsMessageEntity.getId(), List.of(serviceEntity.getId()));
        //Act
        OPSmessageDto retrievedOPSMessageDto = opsController.getSpecificOpsMessage(opsMessageEntity.getId());
        OpsMessageEntity retrievedOpsMessageEntity = EntityDtoMappers.toOpsMessageEntity(retrievedOPSMessageDto);
        //Assert
        Assertions.assertThat(retrievedOpsMessageEntity).isEqualTo(opsMessageEntity);
        Assertions.assertThat(retrievedOPSMessageDto).isEqualTo(opSmessageDto);
    }

    @Test
    void updateSpecificOpsMessage(){
        //Arrange
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(serviceEntity);
        serviceEntity.setId(serviceId);

        List<OpsMessageEntity> opsMessages = SampleData.getNonEmptyListOfOpsMessageEntity(2);
        List<OPSmessageDto> opsMessageDtos = EntityDtoMappers.toOpsMessageDtoShallow(opsMessages);
        UUID opsMessageId0 = opsController.createOpsMessage(opsMessageDtos.get(0)).getId();
        UUID opsMessageId1 = opsController.createOpsMessage(opsMessageDtos.get(1)).getId();
        opsMessages.get(0).setId(opsMessageId0);
        opsMessages.get(1).setId(opsMessageId1);
        opsMessageDtos.get(0).setAffectedServices(List.of(EntityDtoMappers.toServiceDtoShallow(serviceEntity)));

        Map.Entry<OpsMessageEntity, List<ServiceEntity>> before = opsRepository.retrieveOne(opsMessageId0);
        //Act
        opsController.updateSpecificOpsMessage(opsMessageId0 , opsMessageDtos.get(1));
        Map.Entry<OpsMessageEntity, List<ServiceEntity>> after = opsRepository.retrieveOne(opsMessageId0);
        //Assert
        Assertions.assertThat(after.getKey().getId()).isEqualTo(opsMessageId0);
        Assertions.assertThat(after.getKey()).isNotEqualTo(before.getKey());
    }
}