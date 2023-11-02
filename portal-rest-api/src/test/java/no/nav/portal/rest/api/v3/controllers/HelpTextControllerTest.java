package no.nav.portal.rest.api.v3.controllers;

import nav.portal.core.repositories.TestDataSource;
import nav.portal.core.repositories.TestUtil;
import no.portal.web.generated.api.AreaDto;
import no.portal.web.generated.api.IdContainerDto;
import org.assertj.core.api.Assertions;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

public class HelpTextControllerTest {
    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();

    private DbContextConnection connection;

    private final HelpTextController helpTextController = new HelpTextController(dbContext);

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
    void newArea() {
        //Arrange
        //AreaDto areaDto = SampleDataDto.getRandomizedHelpTextDto();
        //Act
        //
        }

}
