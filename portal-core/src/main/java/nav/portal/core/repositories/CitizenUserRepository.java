package nav.portal.core.repositories;

import nav.portal.core.entities.CitizenUserEntity;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextTable;

import java.util.UUID;

public class CitizenUserRepository {

    private final DbContextTable citizenUserTable;


    public CitizenUserRepository(DbContext dbContext) {
        citizenUserTable = dbContext.table("citizen_user");
    }

    public UUID save(CitizenUserEntity entity){
        return citizenUserTable.newSaveBuilderWithUUID("id", entity.getUserID())
                .setField("fornavn", entity.getFornavn())
                .setField("etternavn", entity.getEtternavn())
                .setField("epost", entity.getEpost())
                .setField("tlf", entity.getEpost()).execute().getId();
    }
}
