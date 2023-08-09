package nav.portal.core.enums;

import java.util.Optional;

public enum RecordSource implements DbEnum{
    UNKNOWN("UNKNOWN"),
    GCP_POLL("GCP_POLL"),
    ONPREM_POLL("ONPREM_POLL"),
    PROMETHEUS("PROMETHEUS");

    private String dbRepresentation;

    RecordSource(String dbRepresentation) {
        this.dbRepresentation = dbRepresentation;
    }

    @Override
    public String getDbRepresentation(){
        return dbRepresentation;
    }

    public static Optional<RecordSource> fromDb(String dbRepresentation){
        try{
            return Optional.of((RecordSource) DbEnum.findEnum(dbRepresentation, values(), OpsMessageSeverity.class.getSimpleName()));
        }
        catch (IllegalArgumentException e){
            return Optional.empty();
        }
    }
}
