package nav.portal.core.repositories;

import nav.portal.core.entities.DashboardEntity;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DashboardRepository {


    private final DbContextTable table;

    public DashboardRepository(DbContext dbContext) {
        table = dbContext.table("dashboard");
    }

    public DatabaseSaveResult.SaveStatus save(DashboardEntity entity) {
        DatabaseSaveResult<String> result = table
                .newSaveBuilderWithString("name", entity.getName())
                .setField("areas",entity.getAreasIds())
                .execute();
        return result.getSaveStatus();
    }

    public DashboardEntity retrieve(String name) {
        return table.where("name", name)
                .singleObject(DashboardRepository::toDashboard)
                .orElseThrow(() -> new IllegalArgumentException("Not found: Dashboard with name " + name));
    }

    public List<DashboardEntity> retrieveAll() {
        return table.unordered()
                .stream(DashboardRepository::toDashboard)
                .collect(Collectors.toList());
    }


    public boolean removeArea(String dashboardName, String areaId) {
            DashboardEntity entity = retrieve(dashboardName);
            if(!entity.getAreasIds().contains(areaId)){
                return false;
            }
            entity.removeOne(areaId);
            save(entity);
            return true;
    }

    private static DashboardEntity toDashboard(DatabaseRow row) throws SQLException {
        return new DashboardEntity(row.getString("name"),
                row.getStringList("areas"));
    }

    public void addAreaToDashboard(String dashboardName, String areaId){
        DashboardEntity entity = retrieve(dashboardName);
        entity.addOneArea(areaId);
        save(entity);
    }

    public Query query() {
        return new Query(table.query());
    }

    public static class Query {

        private final DbContextSelectBuilder query;

        public Query(DbContextSelectBuilder query) {
            this.query = query;
        }

        public Stream<DashboardEntity> stream() {
            return query.stream(DashboardRepository::toDashboard);
        }

        private Query query(DbContextSelectBuilder query) {
            return this;
        }
    }
}
