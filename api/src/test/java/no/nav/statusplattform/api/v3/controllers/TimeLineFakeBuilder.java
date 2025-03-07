package no.nav.statusplattform.api.v3.controllers;


import com.fasterxml.jackson.databind.introspect.Annotated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class TimeLineFakeBuilder {

    public TimeLineFake annotateTimeLine(TimeLineFake timeline) {
        Random random = new Random();

        List<TimeLineAnnotation> list = List.of(TimeLineAnnotation.HOLIDAY, TimeLineAnnotation.MAINTENANCE);
        int randomIndex = random.nextInt(2); //Randomly selects 0 or 1

        TimeLineAnnotation annotation = list.get(randomIndex);

        int annotationTimeSpanLength = random.nextInt(5) + 1;

        LinkedHashMap<LocalDate, TimeLineEntryFake> map = timeline.map();

        //1 Select a random starting point
        Random random1 = new Random();
        List<LocalDate> localDates = new ArrayList<>(map.keySet());
        LocalDate startDate = localDates.get(random1.nextInt(localDates.size()));
        int startPoint = localDates.indexOf(startDate);

        //2 loop number of days equaling "annotationTimeSpanLength"
        for (int i = 0; i < annotationTimeSpanLength; i++) {
            if ((startPoint + i) < map.size()) {
                LocalDate date = localDates.get(startPoint);
                TimeLineEntryFake timeLineEntryFake = map.get(date);

                //3 Setting selected day(s) with "annotation"
                map.put(date, new TimeLineEntryFake(
                        annotation,
                        timeLineEntryFake.startTime(),
                        timeLineEntryFake.endTime(),
                        timeLineEntryFake.metadata()
                ));
            }
        }

        return new TimeLineFake(map);

        //return timeline;
    }

    public TimeLineFake build(
            LocalDateTime start,
            LocalDateTime end) {

        LinkedHashMap<LocalDate, TimeLineEntryFake> dateMap = new LinkedHashMap<>();
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        for (LocalDate date = startDate;
             !date.isAfter(endDate);
             date = date.plusDays(1)) {

            //default
            TimeLineAnnotation annotation = TimeLineAnnotation.BASIC;
            LocalDateTime startTime = date.atStartOfDay();

            dateMap.put(date, new TimeLineEntryFake(
                    annotation,
                    startTime,
                    startTime.plusDays(1),
                    new TimeLineEntryMetadata()
            ));
        }
        return new TimeLineFake(dateMap);
    }

    public TimeLineFake build3MonthsFromNowAndBack() {
        return annotateTimeLine(build(LocalDateTime.now().minusMonths(3),
                LocalDateTime.now()));
    }

}
