package nav.portal.core.enums;

import java.util.stream.Stream;

public enum ServiceStatus implements DbEnum {
    OK("ok"),
    DOWN("down"),
    ISSUE("issue");

    private String dbRepresentation;

    ServiceStatus(String dbRepresentation) {
        this.dbRepresentation = dbRepresentation;
    }

    @Override
    public String getDbRepresentation(){
        return dbRepresentation;
    }

    public static ServiceStatus fromDb(String dbRepresentation){
        return (ServiceStatus) DbEnum.findEnum(dbRepresentation.toLowerCase(), values(), ServiceStatus.class.getSimpleName());
    }
}
