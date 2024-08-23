package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.OpeningHoursGroup;

import java.util.List;

record Timeline(List<DayDuringTimeline> days) {

    List<ActualExpectedUptime> actualExpectedUptimesFrom(Records records, OpeningHoursGroup openingHoursGroup) {
        return this.days().stream()
                .map(localDate ->  localDate.dailyUptimeFrom(records))
                .map(dailyUptime -> dailyUptime.apply(openingHoursGroup))
                .toList();
    }
}
