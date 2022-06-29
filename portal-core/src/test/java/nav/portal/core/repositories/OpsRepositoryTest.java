package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.DashboardEntity;
import nav.portal.core.entities.OpsMessageEntity;
import nav.portal.core.entities.ServiceEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.junit.jupiter.api.AfterEach;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OpsRepositoryTest {

    private final DataSource dataSource = TestDataSource.create();
    private final DbContext dbContext = new DbContext();
    private final OpsRepository opsRepository = new OpsRepository(dbContext);
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
    void getAllOpsMessagesForDashboard() {

        //------------------ Arrange ------------------------------------
        //Lager dashboard:
        UUID dashbaordId = dashboardRepository.save("Mitt nye fantastiske dashboard!");

        //Lager område:
        AreaEntity area = SampleData.getRandomizedAreaEntity();
        UUID areaId = areaRepository.save(area);

        //Lager tjeneste:
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        UUID serviceId = serviceRepository.save(serviceEntity);


        //Lager ops Message som IKKE er knyttet til noen tjeneste:

        OpsMessageEntity opsMessageEntity = SampleData.getRandomOpsMessageEntity();
        opsMessageEntity.setId(opsRepository.save(opsMessageEntity,null));

        //------------------ Act ------------------------------------

        //Henter først alle opsmeldinger på dashboardet. Nå skal ingen meldinger ligge på dashboardet.
        List<OpsMessageEntity> shouldBeEmpty = opsRepository.getAllOpsMessagesForDashboard(dashbaordId);
        //Kobler opsmeldingen til tjenesten
        opsRepository.setServicesOnOpsMessage(opsMessageEntity.getId(), List.of(serviceId));
        //Nå skal opsmeldingen være koblet mot dashboardet via tjenesten:
        List<OpsMessageEntity> shouldContainOne = opsRepository.getAllOpsMessagesForDashboard(dashbaordId);



        //------------------ Assert ------------------------------------
        Assertions.assertThat(shouldBeEmpty).isEmpty();
        Assertions.assertThat(shouldContainOne).containsExactly(opsMessageEntity);

    }
}