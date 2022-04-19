package nav.portal.core.repositories;

import nav.portal.core.entities.CitizenUserEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import nav.portal.core.exceptionHandling.ExceptionUtil;
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
        return citizenUserTable.newSaveBuilderWithUUID("id", entity.getUserID())
                .setField("firstName", entity.getFirstName())
                .setField("lastName", entity.getLastName())
                .setField("email", entity.getEmail())
                .setField("phoneNumber", entity.getEmail()).execute().getId();
    }

    public Optional<CitizenUserEntity> retrieve(UUID id) {
        return citizenUserTable.where("userId", id)
                .singleObject(CitizenUserRepository::toCitizenUser);
    }



    static CitizenUserEntity toCitizenUser(DatabaseRow row) {
        try {
            return new CitizenUserEntity()
                    .setUserID(row.getUUID("userId"))
                    .setFirstName(row.getString("firstName"))
                    .setLastName(row.getString("lastName"))
                    .setEmail(row.getString("email"))
                    .setPhoneNumber(row.getString("phoneNumber"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }

    }
}
