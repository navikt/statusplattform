package nav.portal.core.enums;

import java.util.Optional;

public enum OpsMessageSeverity implements DbEnum {
    OK("OK"),
    DOWN("DOWN"),
    ISSUE("ISSUE"),
    NEUTRAL("NEUTRAL");

    private String dbRepresentation;

    OpsMessageSeverity(String dbRepresentation) {
        this.dbRepresentation = dbRepresentation;
    }

    @Override
    public String getDbRepresentation(){
        return dbRepresentation;
    }

    public static Optional<OpsMessageSeverity> fromDb(String dbRepresentation){
        try{
            return Optional.of((OpsMessageSeverity) DbEnum.findEnum(dbRepresentation, values(), OpsMessageSeverity.class.getSimpleName()));
        }
        catch (IllegalArgumentException e){
            return Optional.empty();
        }
    }
}
