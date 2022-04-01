package nav.portal.core.entities;

import java.sql.Time;
import java.util.Objects;
import java.util.UUID;

public class OpeningHoursEntity {

    private UUID id;
    private UUID service_id;
    private int day_of_the_week;
    private Time opening_time;
    private Time closing_time;


    public OpeningHoursEntity() {
    }

    public UUID getId() {
        return id;
    }

    public OpeningHoursEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getService_id() {
        return service_id;
    }

    public OpeningHoursEntity setService_id(UUID service_id) {
        this.service_id = service_id;
        return this;
    }

    public int getDay_of_the_week() {
        return day_of_the_week;
    }

    public OpeningHoursEntity setDay_of_the_week(int day_of_the_week) {
        this.day_of_the_week = day_of_the_week;
        return this;
    }

    public Time getOpening_time() {
        return opening_time;
    }

    public OpeningHoursEntity setOpening_time(Time opening_time) {
        this.opening_time = opening_time;
        return this;
    }

    public Time getClosing_time() {
        return closing_time;
    }

    public OpeningHoursEntity setClosing_time(Time closing_time) {
        this.closing_time = closing_time;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpeningHoursEntity that = (OpeningHoursEntity) o;
        return day_of_the_week == that.day_of_the_week && id.equals(that.id) && service_id.equals(that.service_id) && opening_time.equals(that.opening_time) && closing_time.equals(that.closing_time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, service_id, day_of_the_week, opening_time, closing_time);
    }
}
