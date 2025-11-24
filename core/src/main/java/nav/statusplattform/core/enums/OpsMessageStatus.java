package nav.statusplattform.core.enums;

import java.util.Optional;

public enum OpsMessageStatus implements DbEnum {
    EXAMINING("EXAMINING"),
    MAITENANCE("MAITENANCE"),
    SOLVING("SOLVING"),
    SOLVED("SOLVED");

    private String dbRepresentation;

    OpsMessageStatus(String dbRepresentation) {
        this.dbRepresentation = dbRepresentation;
    }

    @Override
    public String getDbRepresentation(){
        return dbRepresentation;
    }

    public static Optional<OpsMessageStatus> fromDb(String dbRepresentation){
        try{
            return Optional.of((OpsMessageStatus) DbEnum.findEnum(dbRepresentation, values(), OpsMessageStatus.class.getSimpleName()));
        }
        catch (IllegalArgumentException e){
            return Optional.empty();
        }
    }
}