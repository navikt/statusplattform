package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import org.fluentjdbc.*;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;




public class AreaRepository {

    private final DbContextTable table;

    public AreaRepository(DbContext dbContext) {
        table = dbContext.table("area");
    }

    public DatabaseSaveResult.SaveStatus save(AreaEntity entity) {
        DatabaseSaveResult<String> result = table.newSaveBuilderWithString("id", entity.getId())
                .setField("name",entity.getName())
                .setField("beskrivelse", entity.getBeskrivelse())
                .setField("ikon", entity.getIkon())
                .setField("rangering", entity.getRangering())
                .setField("services", entity.getServisesIds())
                .execute();
        return result.getSaveStatus();
    }

    public AreaEntity retrieve(String id) {
        return table.where("id", id)
                .singleObject(AreaRepository::toArea)
                .orElseThrow(() -> new IllegalArgumentException("Not found: Area with id " + id));
    }

    public void addServiceToArea(String areaId, String serviceId) {
        AreaEntity entity = retrieve(areaId);
        entity.addService(serviceId);
        save(entity);
    }

    public void removeServiceFromArea(String areaId, String serviceId) {
        AreaEntity entity = retrieve(areaId);
        entity.removeService(serviceId);
        save(entity);
    }

    public List<AreaEntity> retrieve(List<String> ids) {
        return table.whereIn("id", ids)
                .stream(AreaRepository::toArea)
                .collect(Collectors.toList());
    }


    private static AreaEntity toArea(DatabaseRow row) throws SQLException {
        return new AreaEntity(row.getString("id"),
                row.getString("name"),
                row.getString("beskrivelse"),
                row.getString("ikon"),
                row.getInt("rangering"),
                row.getStringList("services")
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

        public Stream<AreaEntity> stream() {
            return query.stream(AreaRepository::toArea);
        }

        private Query query(DbContextSelectBuilder query) {
            return this;
        }
    }
}
