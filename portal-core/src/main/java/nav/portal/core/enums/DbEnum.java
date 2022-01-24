package nav.portal.core.enums;

import java.util.stream.Stream;

public interface DbEnum {
    String getDbRepresentation();

    static DbEnum findEnum(String dbRepresentation, DbEnum[] values, String simpleName) {
        return Stream.of(values)
                .filter(serviceStatus -> serviceStatus.getDbRepresentation().equals(dbRepresentation))
                .findAny()
                .orElseThrow(() -> {
                    String errorMessage = String.format("dbRepresentation %s not fpund in enum %s",
                            dbRepresentation,
                            simpleName);
                    return new IllegalArgumentException(errorMessage);
                });
    }
}
