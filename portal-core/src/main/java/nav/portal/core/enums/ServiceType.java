package nav.portal.core.enums;

public enum ServiceType implements DbEnum {
    APPLIKASJON("APPLIKASJON");

    private String dbRepresentation;

    ServiceType(String dbRepresentation) {
        this.dbRepresentation = dbRepresentation;
    }

    @Override
    public String getDbRepresentation(){
        return dbRepresentation;
    }

    public static ServiceType fromDb(String dbRepresentation){
        return (ServiceType) DbEnum.findEnum(dbRepresentation.toUpperCase(), values(), ServiceType.class.getSimpleName());
    }
}
