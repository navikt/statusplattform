package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.CitizenUserEntity;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import org.fluentjdbc.DatabaseRow;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextTable;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class CitizenUserRepository {

    private final DbContextTable citizenUserTable;


    public CitizenUserRepository(DbContext dbContext) {
        citizenUserTable = dbContext.table("citizen_user");
    }

    public UUID save(CitizenUserEntity entity){
        return citizenUserTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("firstName", entity.getFirstName())
                .setField("lastName", entity.getLastName())
                .setField("email", entity.getEmail())
                .setField("phoneNumber", entity.getPhoneNumber()).execute().getId();
    }

    public Optional<CitizenUserEntity> retrieve(UUID id) {
        return citizenUserTable.where("id", id)
                .singleObject(CitizenUserRepository::toCitizenUser);
    }



    static CitizenUserEntity toCitizenUser(DatabaseRow row) {
        try {
            return new CitizenUserEntity()
                    .setId(row.getUUID("id"))
                    .setFirstName(row.getString("firstName"))
                    .setLastName(row.getString("lastName"))
                    .setEmail(row.getString("email"))
                    .setPhoneNumber(row.getString("phoneNumber"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }

    }
}
