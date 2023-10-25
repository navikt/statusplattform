package nav.portal.core.repositories;

import nav.portal.core.entities.AreaEntity;
import nav.portal.core.entities.HelpTextEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceType;
import nav.portal.core.exceptionHandling.ExceptionUtil;
import org.actioncontroller.HttpRequestException;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class HelpTextRepository {
    private final DbContextTable help_textTable;
    private final DbContext dbContext;

    public HelpTextRepository(DbContext dbContext) {
        help_textTable = dbContext.table("help_text");
        this.dbContext = dbContext;
    }

    public Long save(HelpTextEntity helpText) {
        //Sjekk på nr +type kombinasjon
        if (help_textTable.where("number", (long)helpText.getNr())
                .where("type", helpText.getType()).getCount() > 0) {
            throw new HttpRequestException("Hjelpetekst med nummer: " + helpText.getNr()
                    + ", og type: " + helpText.getType() + " finnes allerede");
        }

        return help_textTable.newSaveBuilder("number", (long)helpText.getNr())
                .setField("type", helpText.getType().getDbRepresentation())
                .setField("content", helpText.getContent())
                .execute()
                .getId();
    }

    public void update(HelpTextEntity helpText){
        help_textTable.where("number",(long)helpText.getNr())
                .update()
                .setField("number", helpText.getNr())
                .setField("type", helpText.getType().getDbRepresentation())
                .setField("content", helpText.getContent())
                .execute();
    }

    public Optional<HelpTextEntity> retrieve(long nr, ServiceType serviceType) {
        return help_textTable
                .where("number", (long) nr)
                .where("type", serviceType )
                .singleObject(HelpTextRepository::toHelpText);
    }

    public List<HelpTextEntity> retrieveAllHelpTextEntity() {
        return  help_textTable.orderedBy("number")
                .stream(HelpTextRepository::toHelpText)
                .collect(Collectors.toList());
    }

    public  List<HelpTextEntity> retrieveAllWithType(ServiceType serviceType) {
        return  help_textTable.orderedBy("number")
                .where("service.type", serviceType.getDbRepresentation())
                .stream(HelpTextRepository::toHelpText)
                .collect(Collectors.toList());
    }

    public  List<HelpTextEntity> retrieveAllWithServiceType() {
        return  help_textTable.orderedBy("number")
                .where("service.type", ServiceType.TJENESTE.getDbRepresentation())
                .stream(HelpTextRepository::toHelpText)
                .collect(Collectors.toList());
    }

    public  List<HelpTextEntity> retrieveAllWithComponentType() {
        return  help_textTable
                .where("service.type",  ServiceType.KOMPONENT.getDbRepresentation())
                .where("service.type",  ServiceType.TJENESTE.getDbRepresentation())
                .stream(HelpTextRepository::toHelpText)
                .collect(Collectors.toList());
    }

    public boolean delete(int nr, ServiceType serviceType) {
        help_textTable.where("number", (long)nr)
                .where("type", serviceType )
                .executeDelete();
        return true;
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
