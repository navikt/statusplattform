package nav.portal.core.repositories;

import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextTable;

public class TestUtil {

    static void clearAllTableData(DbContext dbContext){
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
}
