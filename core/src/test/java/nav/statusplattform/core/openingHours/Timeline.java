package nav.statusplattform.core.openingHours;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record Timeline(List<LocalDate> days) {

    public static Timeline from(TimeSpan timeSpan) {
        return new Timeline(
                timeSpan.from().toLocalDate()
                        .datesUntil(timeSpan.to().toLocalDate().plusDays(1))
                        .collect(Collectors.toList()));
    }
}
