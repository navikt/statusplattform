package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.OpsMessageEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.OpsControllerHelper;
import no.portal.web.generated.api.OPSmessageDto;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class OpsControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private final OpsController opsController = new OpsController(dbContext);
    private final OpsControllerHelper opsControllerHelper = new OpsControllerHelper(dbContext);
    private final OpsRepository opsRepository = new OpsRepository(dbContext);

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
        OpsMessageEntity opsMessageEntity = SampleData.getRandomOpsMessageEntity();
        OPSmessageDto opsMessageDto = EntityDtoMappers.toOpsMessageDtoShallow(opsMessageEntity);

        //Act
        UUID opsMessageId = opsController.createOpsMessage(opsMessageDto).getId();
        opsMessageEntity.setId(opsMessageId);
        OpsMessageEntity retrievedEntity = opsRepository.retrieveOne(opsMessageId).getKey();

        //Assert
        Assertions.assertThat(retrievedEntity).isEqualTo(opsMessageEntity);
    }

    @Test
    void createMaintenanceMessage() {
    }


    @Test
    void getAllOpsMessages() {
        //Arrange
        List<OpsMessageEntity> opsMessagesEntitiesList = SampleData.getNonEmptyListOfOpsMessageEntity(3);
        List<OPSmessageDto> opsMessagesDtoList = opsMessagesEntitiesList.stream().map(EntityDtoMappers::toOpsMessageDtoShallow).collect(Collectors.toList());

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
}