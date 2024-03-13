package nav.statusplattform.core.entities;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class DailyStatusAggregationForServiceEntity {
    private UUID id;
    private UUID service_id;
    private LocalDate aggregation_date;
    private String Information;
    private int number_of_status_ok;
    private int number_of_status_issue;
    private int number_of_status_down;

    public DailyStatusAggregationForServiceEntity() {
    }

    public UUID getId() {
        return id;
    }

    public DailyStatusAggregationForServiceEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getInformation() {
        return Information;
    }

    public UUID getService_id() {
        return service_id;
    }

    public DailyStatusAggregationForServiceEntity setService_id(UUID service_id) {
        this.service_id = service_id;
        return this;
    }

    public LocalDate getAggregation_date() {
        return aggregation_date;
    }

    public DailyStatusAggregationForServiceEntity setAggregation_date(LocalDate aggregation_date) {
        this.aggregation_date = aggregation_date;
        return this;
    }

    public int getNumber_of_status_ok() {
        return number_of_status_ok;
    }

    public DailyStatusAggregationForServiceEntity setNumber_of_status_ok(int number_of_status_ok) {
        this.number_of_status_ok = number_of_status_ok;
        return this;
    }

    public int getNumber_of_status_issue() {
        return number_of_status_issue;
    }

    public DailyStatusAggregationForServiceEntity setNumber_of_status_issue(int number_of_status_issue) {
        this.number_of_status_issue = number_of_status_issue;
        return this;
    }

    public int getNumber_of_status_down() {
        return number_of_status_down;
    }

    public DailyStatusAggregationForServiceEntity setNumber_of_status_down(int number_of_status_down) {
        this.number_of_status_down = number_of_status_down;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyStatusAggregationForServiceEntity that = (DailyStatusAggregationForServiceEntity) o;
        return number_of_status_ok == that.number_of_status_ok && number_of_status_issue == that.number_of_status_issue && number_of_status_down == that.number_of_status_down && Objects.equals(id, that.id) && Objects.equals(service_id, that.service_id) && Objects.equals(aggregation_date, that.aggregation_date) && Objects.equals(Information, that.Information);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, service_id, aggregation_date, Information, number_of_status_ok, number_of_status_issue, number_of_status_down);
    }
}
