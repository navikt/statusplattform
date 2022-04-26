package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.entities.OpsMessageEntity;
import nav.portal.core.repositories.*;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.OpsControllerHelper;
import no.nav.portal.rest.api.Helpers.ServiceControllerHelper;
import no.portal.web.generated.api.OPSmessageDto;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.UUID;

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
}