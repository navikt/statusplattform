package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.HelpTextEntity;
import nav.statusplattform.core.enums.ServiceType;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import org.actioncontroller.HttpRequestException;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class HelpTextRepository {
    private final DbContextTable help_textTable;


    public HelpTextRepository(DbContext dbContext) {
        help_textTable = dbContext.table("help_text");

    }

    public HelpTextEntity save(HelpTextEntity helpText) throws HttpRequestException {
        //Check if input is valid, and no duplicates
        //Sjekk på nummer+type kombinasjon
        if(help_textTable.where("number",helpText.getNumber())
                .where("type", helpText.getType()).getCount()>0){
            throw new HttpRequestException("Hjelpe tekst med nummer: " +
                    helpText.getNumber() + ", og type: " + helpText.getType() +
                    " finnes allerede");
        }
        help_textTable.insert()
                .setField("number", (long)helpText.getNumber())
                .setField("type", helpText.getType().getDbRepresentation())
                .setField("content", helpText.getContent())
                .execute();

        return help_textTable
                .where("number", helpText.getNumber())
                .where("type", helpText.getType().getDbRepresentation())
                .singleObject(HelpTextRepository::toHelpText)
                .orElseThrow(() -> new IllegalArgumentException
                        ("Not found: HelpText nr: " + helpText.getNumber()
                                + ", type: " + helpText.getContent()));
    }

    public void update(HelpTextEntity helpText){
        help_textTable.where("number",(long)helpText.getNumber())
                .where("type", helpText.getType().getDbRepresentation())
                .update()
                .setField("number", helpText.getNumber())
                .setField("type", helpText.getType().getDbRepresentation())
                .setField("content", helpText.getContent())
                .execute();
    }

    public HelpTextEntity retrieve(long nr, ServiceType serviceType) {
        return help_textTable
                .where("number", nr)
                .where("type", serviceType)
                .singleObject(HelpTextRepository::toHelpText)
                .orElseThrow(() -> new IllegalArgumentException
                        ("Not found: HelpText nr: " + (int) nr
                                + ", type: " + serviceType));
    }

    public List<HelpTextEntity> retrieveAllHelpTexts() {
        return  help_textTable.orderedBy("number")
                .stream(HelpTextRepository::toHelpText)
                .collect(Collectors.toList());
    }

    public  List<HelpTextEntity> retrieveHelpTextServices() {
        return  help_textTable.orderedBy("number")
                .where("type", ServiceType.TJENESTE.getDbRepresentation())
                .stream(HelpTextRepository::toHelpText)
                .collect(Collectors.toList());
    }

    public  List<HelpTextEntity> retrieveHelpTextComponents() {
        return  help_textTable
                .where("type",  ServiceType.KOMPONENT.getDbRepresentation())
                .stream(HelpTextRepository::toHelpText)
                .collect(Collectors.toList());
    }

    public int delete(HelpTextEntity helpText) {
        return help_textTable.where("number",(long)helpText.getNumber())
                .where("type", helpText.getType().getDbRepresentation())
                .executeDelete();
    }

    static HelpTextEntity toHelpText(DatabaseRow row) {
        try {
            return new HelpTextEntity()
                    .setNumber(row.getInt("number"))
                    .setType(ServiceType.fromDb(row.getString("type")))
                    .setContent(row.getString("content"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }
}
