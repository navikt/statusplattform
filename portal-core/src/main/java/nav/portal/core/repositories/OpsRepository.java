package nav.portal.core.repositories;

import nav.portal.core.entities.OpsMessageEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.exceptionHandling.ExceptionUtil;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.*;

public class OpsRepository {


    private final DbContextTable opsMessageTable;
    private final DbContextTable opsMessageServiceTable;
    private final DbContextTable serviceTable;


    public OpsRepository(DbContext dbContext) {
        opsMessageTable = dbContext.table("ops_message");
        serviceTable = dbContext.table("service");
        opsMessageServiceTable = dbContext.table("ops_message_service");
    }

    public UUID save(OpsMessageEntity entity, List<UUID> services) {
        DatabaseSaveResult<UUID> result = opsMessageTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("intern_header", entity.getInternalHeader())
                .setField("intern_text", entity.getInternalText())
                .setField("extern_header", entity.getExternalHeader())
                .setField("extern_text", entity.getExternalText())
                .setField("is_active", entity.getIsActive())
                .setField("only_show_for_nav_employees", entity.getOnlyShowForNavEmployees())
                .execute();
        setServicesOnOpsMessage(result.getId(), services);
        return result.getId();
    }

    public void setServicesOnOpsMessage(UUID opsId, List<UUID> services) {
        opsMessageServiceTable.where("ops_message_id", opsId).executeDelete();

        for(int i = 0; i < services.size(); i++){
            opsMessageServiceTable.insert()
                    .setField("ops_message_id", opsId)
                    .setField("service_id", services.get(i))
                    .execute();
        }
    }

    public Map.Entry<OpsMessageEntity, List<ServiceEntity>> retrieveOne(UUID ops_id) {
        DbContextTableAlias ops = opsMessageTable.alias("ops");
        DbContextTableAlias o2s = opsMessageServiceTable.alias("o2s");
        DbContextTableAlias service = serviceTable.alias("service");

        Map<OpsMessageEntity, List<ServiceEntity>> result = new HashMap<>();
        ops
                .where("id" , ops_id)
                .leftJoin(ops.column("id"), o2s.column("ops_message_id"))
                .leftJoin(o2s.column("service_id"), service.column("id"))
                .list(row -> {
                    List<ServiceEntity> serviceList = result
                            .computeIfAbsent(toOps(row.table(ops)), ignored -> new ArrayList<>());

                    DatabaseRow serivceRow = row.table(service);
                    Optional.ofNullable(row.getUUID("service_id"))
                            .ifPresent(serviceId -> serviceList.add(ServiceRepository.toService(serivceRow)));
                    return null;
                });
        return result
                .entrySet()
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not found: OpsMessage with id " + ops_id));
    }

    static OpsMessageEntity toOps(DatabaseRow row){
        try {
            return new OpsMessageEntity()
                    .setId(row.getUUID("id"))
                    .setInternalHeader(row.getString("intern_header"))
                    .setInternalText(row.getString("intern_text"))
                    .setExternalHeader(row.getString("extern_header"))
                    .setExternalText(row.getString("extern_text"))
                    .setIsActive(row.getBoolean("is_active"))
                    .setOnlyShowForNavEmployees(row.getBoolean("only_show_for_nav_employees"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }
}
