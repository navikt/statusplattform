package nav.portal.core.entities;

import java.util.Date;
import java.util.UUID;

public class DailyStatusAggregationForServiceEntity {
    private UUID id;
    private UUID service_id;
    private Date aggregation_date;
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

    public UUID getService_id() {
        return service_id;
    }

    public DailyStatusAggregationForServiceEntity setService_id(UUID service_id) {
        this.service_id = service_id;
        return this;
    }

    public Date getAggregation_date() {
        return aggregation_date;
    }

    public DailyStatusAggregationForServiceEntity setAggregation_date(Date aggregation_date) {
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
}
