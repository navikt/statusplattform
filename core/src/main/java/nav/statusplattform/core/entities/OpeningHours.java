package nav.statusplattform.core.entities;

import java.time.LocalTime;

public record OpeningHours(LocalTime startTime, LocalTime endTime) {
}
