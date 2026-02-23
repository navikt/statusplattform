package nav.statusplattform.core.enums;

import java.util.Optional;

public enum NotificationStatus implements DbEnum {
    PENDING("PENDING"),
    SENT("SENT"),
    FAILED("FAILED");

    private String dbRepresentation;

    NotificationStatus(String dbRepresentation) {
        this.dbRepresentation = dbRepresentation;
    }

    @Override
    public String getDbRepresentation() {
        return dbRepresentation;
    }

    public static Optional<NotificationStatus> fromDb(String dbRepresentation) {
        try {
            return Optional.of((NotificationStatus) DbEnum.findEnum(dbRepresentation, values(), NotificationStatus.class.getSimpleName()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
