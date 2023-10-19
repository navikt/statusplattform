package nav.portal.core.repositories;

import nav.portal.core.entities.HelpTextEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import nav.portal.core.exceptionHandling.ExceptionUtil;
import org.actioncontroller.HttpRequestException;
import org.fluentjdbc.DatabaseRow;
import org.fluentjdbc.DatabaseTable;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextTable;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HelpTextRepository {
    private final DbContextTable help_textTable;

    public HelpTextRepository(DbContext dbContext) {
        help_textTable = dbContext.table("help_text");
    }

    public Long save(HelpTextEntity helpText) {
        //Sjekk på nr +type kombinasjon
        if (help_textTable.where("number", helpText.getNr())
                .where("deleted", false)
                .where("type", helpText.getType()).getCount() > 0) {
            throw new HttpRequestException("Hjelpetekst med nummer: " + helpText.getNr()
                    + ", og type: " + helpText.getType() + " finnes allerede");
        }
        return help_textTable.newSaveBuilder("id", helpText.getNr())
                .setField("number", helpText.getNr())
                .setField("type", helpText.getType().getDbRepresentation())
                .setField("content", helpText.getContent())
                .execute()
                .getId();
    }

    public void update(HelpTextEntity helpText){
        help_textTable.where("number",helpText.getNr())
                .update()
                .setField("number", helpText.getNr())
                .setField("type", helpText.getType().getDbRepresentation())
                .setField("content", helpText.getContent())
                .execute();
    }

    public Optional<HelpTextEntity> retrieve(long nr) {
        return help_textTable.where("number", nr)
                .singleObject(HelpTextRepository::toHelpText);
    }

    public List<HelpTextEntity> retrieveAllHelpTextEntityShallow() {
        return  help_textTable.where("deleted",Boolean.FALSE).list(HelpTextRepository::toHelpText);
    }

    public void delete(long nr) {
        help_textTable.where("number", nr)
                .update()
                .setField("deleted", Boolean.TRUE)
                .execute();
    }

    static HelpTextEntity toHelpText(DatabaseRow row) {
        try {
            return new HelpTextEntity()
                    .setNr(row.getInt("number"))
                    .setType(ServiceType.fromDb(row.getString("type")))
                    .setContent(row.getString("content"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }

    }


}
