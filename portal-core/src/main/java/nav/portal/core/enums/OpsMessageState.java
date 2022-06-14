package nav.portal.core.enums;

import java.util.Optional;

public enum OpsMessageState implements DbEnum {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    ARCHIVED("ARCHIVED");

    private String dbRepresentation;

    OpsMessageState(String dbRepresentation) {
        this.dbRepresentation = dbRepresentation;
    }

    @Override
    public String getDbRepresentation(){
        return dbRepresentation;
    }

    public static Optional<OpsMessageState> fromDb(String dbRepresentation){
        try{
            return Optional.of((OpsMessageState) DbEnum.findEnum(dbRepresentation, values(), OpsMessageState.class.getSimpleName()));
        }
        catch (IllegalArgumentException e){
            return Optional.empty();
        }
    }
}
