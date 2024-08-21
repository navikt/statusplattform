package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;

import java.util.List;
import java.util.stream.Collectors;

public record Timeline(List<DayDuringTimeline> days) {

    //TODO: Remove this function
    public static Timeline from(TimeSpan timeSpan) {
        return new Timeline(
                timeSpan.from().toLocalDate()
                        .datesUntil(timeSpan.to().toLocalDate().plusDays(1))
                        .map(localDate -> new DayDuringTimeline(localDate))
                        .collect(Collectors.toList()));
    }

    List<ActualExpectedUptime> actualExpectedUptimesFrom(Records records, OpeningHoursGroup openingHoursGroup) {
        return this.days().stream()
                .map(localDate ->  localDate.dailyUptimeFrom(records))
                .map(dailyUptime -> dailyUptime.apply(openingHoursGroup))
                .toList();
    }
}
