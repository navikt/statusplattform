package nav.statusplattform.core.enums;

import java.util.Optional;

public enum NotificationType implements DbEnum {
    OPS_MESSAGE("OPS_MESSAGE"),
    STATUS_CHANGE("STATUS_CHANGE"),
    MAINTENANCE("MAINTENANCE");

    private String dbRepresentation;

    NotificationType(String dbRepresentation) {
        this.dbRepresentation = dbRepresentation;
    }

    @Override
    public String getDbRepresentation() {
        return dbRepresentation;
    }

    public static Optional<NotificationType> fromDb(String dbRepresentation) {
        try {
            return Optional.of((NotificationType) DbEnum.findEnum(dbRepresentation, values(), NotificationType.class.getSimpleName()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
