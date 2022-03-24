package nav.portal.core.repositories;

import nav.portal.core.entities.RecordEntity;
import org.fluentjdbc.DatabaseSaveResult;
import org.fluentjdbc.DatabaseTableImpl;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextTable;

import java.util.UUID;

public class TestUtil {

    public static void clearAllTableData(DbContext dbContext){
        DbContextTable serviceServiceTable = dbContext.table("service_service");
        serviceServiceTable.whereExpression("service1_id is NOT null").executeDelete();

        DbContextTable serviceStatusTable = dbContext.table("service_status");
        serviceStatusTable.whereExpression("id is NOT null").executeDelete();

        DbContextTable areaServiceTable = dbContext.table("area_service");
        areaServiceTable.whereExpression("service_id is NOT null").executeDelete();

        DbContextTable dashboardAreaTable = dbContext.table("dashboard_area");
        dashboardAreaTable.whereExpression("dashboard_id is NOT null").executeDelete();

        DbContextTable serviceTable = dbContext.table("service");
        serviceTable.whereExpression("id is NOT null").executeDelete();

        DbContextTable areaTable = dbContext.table("area");
        areaTable.whereExpression("id is NOT null").executeDelete();

        DbContextTable dashboardTable = dbContext.table("dashboard");
        dashboardTable.whereExpression("id is NOT null").executeDelete();
    }


    public static UUID saveRecordBackInTime(RecordEntity entity, DbContext dbContext) {
        DbContextTable recordTable = dbContext.table(new DatabaseTableImpl("service_status"));
        DatabaseSaveResult<UUID> result = recordTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("service_id", entity.getServiceId())
                .setField("created_at", entity.getCreated_at())
                .setField("status", entity.getStatus())
                .setField("description", "GENERATED MOCK")
                .setField("logglink", entity.getLogglink())
                .setField("response_time", entity.getResponsetime())
                .execute();
        return result.getId();
    }
}
