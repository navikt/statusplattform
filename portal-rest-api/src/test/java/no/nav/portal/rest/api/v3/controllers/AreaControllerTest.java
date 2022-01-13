package no.nav.portal.rest.api.v3.controllers;

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

import no.nav.portal.rest.api.Helpers.AreaRepositoryHelper;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;


import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;


import static no.nav.portal.rest.api.EntityDtoMappers.toAreaDtoShallow;
import static org.junit.jupiter.api.Assertions.*;

class AreaControllerTest {

    private final SampleData sampleData = new SampleData();
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final AreaController areaController = new AreaController(dbContext);
    private final AreaRepository areaRepository = new AreaRepository(dbContext);
    private final ServiceController serviceController = new ServiceController(dbContext);
    private final ServiceRepository serviceRepository = new ServiceRepository(dbContext);
    private final RecordRepository recordRepository = new RecordRepository(dbContext);


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
    void getAllAreas() {
        //Arrange
        AreaEntity area = sampleData.getRandomizedAreaEntity();
        //Act

        //Assert


    }

    @Test
    void newArea() {
        //Arrange
        AreaEntity area = sampleData.getRandomizedAreaEntity();
        //Act
        UUID uuid = areaController.newArea(EntityDtoMappers.toAreaDtoShallow(area));
        //Assert
        Assertions.assertThat(uuid).isNotNull();
    }

    @Test
    void updateArea() {
    }

    @Test
    void deleteArea() {
    }

    @Test
    void getAreas() {
    }

    @Test
    void addServiceToArea() {
    }

    @Test
    void removeServiceFromArea() {
    }
}