package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.ExampleEntity;
import nav.portal.core.entities.ServiceEntity;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceRepository {

    private final DbContextTable table;

    public ServiceRepository(DbContext dbContext) {
        table = dbContext.table("service");
    }

    public DatabaseSaveResult.SaveStatus save(ServiceEntity entity) {
        DatabaseSaveResult<String> result = table.newSaveBuilderWithString("id", entity.getId())
                .setField("name", entity.getName())
                .setField("type", entity.getType())
                .setField("team", entity.getTeam())
                .setField("dependencies", entity.getDependencies())
                .setField("monitorlink", entity.getMonitorlink())
                .setField("description", entity.getDescription())
                .setField("logglink", entity.getLogglink())
                .execute();
        return result.getSaveStatus();
    }

    public Optional<ServiceEntity> retrieve(String id) {
        return table.where("id", id)
                .singleObject(ServiceRepository::toService);
    }

    public Boolean doesEntryExist(String id){
        return table.where("id", id)
                .singleObject(ServiceRepository::toService).isPresent();
    }
    public List<ServiceEntity> retrieve(List<String> ids) {
        return table.whereIn("id", ids)
                .stream(ServiceRepository::toService)
                .collect(Collectors.toList());
    }

    public int delete(String id) {
        return table.where("id", id).executeDelete();
    }

    public List<ServiceEntity> retrieveAll() {
        return table.unordered()
                .stream(ServiceRepository::toService)
                .collect(Collectors.toList());
    }


    private static ServiceEntity toService(DatabaseRow row) throws SQLException {
        return new ServiceEntity(row.getString("name"),
                row.getString("id"),
                row.getString("type"),
                row.getString("team"),
                row.getStringList("dependencies"),
                row.getString("monitorlink"),
                row.getString("description"),
                row.getString("logglink")
                );

    }

    public Query query() {
        return new Query(table.query());
    }

    public static class Query {

        private final DbContextSelectBuilder query;

        public Query(DbContextSelectBuilder query) {
            this.query = query;
        }

        public Stream<ServiceEntity> stream() {
            return query.stream(ServiceRepository::toService);
        }

        private Query query(DbContextSelectBuilder query) {
            return this;
        }
    }
}
