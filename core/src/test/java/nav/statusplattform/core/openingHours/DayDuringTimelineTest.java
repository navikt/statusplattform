package nav.statusplattform.core.openingHours;

import nav.statusplattform.core.entities.RecordEntity;
import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.SampleData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DayDuringTimelineTest {

    @Test
    public void when_there_is_only_one_record() {

    }

    @Test
    public void when_service_is_up_then_goes_down_during_the_day() {
        Records records = Records.fromRecordEntities(
                List.of(
                        getRecordEntity(LocalDate.of(2024, 7, 1).atTime(11, 14, 3), ServiceStatus.OK),
                        getRecordEntity(LocalDate.of(2024, 8, 1).atTime(14, 13), ServiceStatus.DOWN)
                ), new TimeSpan(
                        LocalDate.of(2024, 7, 1).atTime(11, 14, 3),
                        LocalDate.of(2024, 8, 2).atTime(14, 13)));

        DayDuringTimeline dayDuringTimeline = new DayDuringTimeline(LocalDate.of(2024, 8, 1));
        DailyUptime dailyUptime = dayDuringTimeline.dailyUptimeFrom(records);

        assertThat(dailyUptime.serviceDowns()).contains(new ServiceDown(
                LocalDate.of(2024, 8, 1).atTime(14, 13),
                LocalDate.of(2024, 8, 1).atTime(23, 59, 59, 999999999)));
    }

    @Test
    public void when_service_becomes_down_and_comes_up_later_the_same_day() {
        Records records = Records.fromRecordEntities(
                List.of(
                        getRecordEntity(LocalDate.of(2024, 8, 1).atTime(11, 14, 3), ServiceStatus.DOWN),
                        getRecordEntity(LocalDate.of(2024, 8, 1).atTime(14, 13), ServiceStatus.OK)
                ), new TimeSpan(
                        LocalDate.of(2024, 7, 1).atTime(11, 14, 3),
                        LocalDate.of(2024, 8, 1).atTime(14, 13)));

        DayDuringTimeline dayDuringTimeline = new DayDuringTimeline(LocalDate.of(2024, 8, 1));
        DailyUptime dailyUptime = dayDuringTimeline.dailyUptimeFrom(records);

        assertThat(dailyUptime.serviceDowns()).contains(new ServiceDown(
                LocalDate.of(2024, 8, 1).atTime(11, 14, 3),
                LocalDate.of(2024, 8, 1).atTime(14, 13)));
    }

    @Test
    public void when_service_is_down_and_comes_up_later_the_same_day() {
        Records records = Records.fromRecordEntities(
                List.of(
                        getRecordEntity(LocalDate.of(2024, 8, 1).atStartOfDay(), ServiceStatus.DOWN),
                        getRecordEntity(LocalDate.of(2024, 8, 1).atTime(14, 13), ServiceStatus.OK)
                ), new TimeSpan(
                        LocalDate.of(2024, 7, 1).atTime(11, 14, 3),
                        LocalDate.of(2024, 8, 1).atTime(14, 13)));

        DayDuringTimeline dayDuringTimeline = new DayDuringTimeline(LocalDate.of(2024, 8, 1));
        DailyUptime dailyUptime = dayDuringTimeline.dailyUptimeFrom(records);

        assertThat(dailyUptime.serviceDowns()).contains(new ServiceDown(
                LocalDate.of(2024, 8, 1).atStartOfDay(),
                LocalDate.of(2024, 8, 1).atTime(14, 13)));
    }

    @Test
    public void when_service_is_down_and_does_not_come_up_before_next_day_the_service_is_down_until_the_end_of_day() {
        Records records = Records.fromRecordEntities(
                List.of(
                        getRecordEntity(LocalDate.of(2024, 8, 1).atStartOfDay(), ServiceStatus.DOWN),
                        getRecordEntity(LocalDate.of(2024, 8, 3).atStartOfDay(), ServiceStatus.OK)
                ), new TimeSpan(
                        LocalDate.of(2024, 7, 1).atTime(11, 14, 3),
                        LocalDate.of(2024, 8, 1).atTime(14, 13)));

        DayDuringTimeline dayDuringTimeline = new DayDuringTimeline(LocalDate.of(2024, 8, 1));
        DailyUptime dailyUptime = dayDuringTimeline.dailyUptimeFrom(records);

        assertThat(dailyUptime.serviceDowns()).contains(new ServiceDown(
                LocalDate.of(2024, 8, 1).atStartOfDay(),
                LocalDate.of(2024, 8, 1).atTime(23, 59, 59, 999999999)));
    }

    private static RecordEntity getRecordEntity(LocalDateTime createdAt, ServiceStatus serviceStatus) {
        ServiceEntity serviceEntity = SampleData.getRandomizedServiceEntity();
        RecordEntity randomizedRecordEntityForService = SampleData.getRandomizedRecordEntityForService(serviceEntity);
        randomizedRecordEntityForService.setCreated_at(createdAt.atZone(ZoneId.systemDefault()));
        randomizedRecordEntityForService.setStatus(serviceStatus);
        return randomizedRecordEntityForService;
    }
}
