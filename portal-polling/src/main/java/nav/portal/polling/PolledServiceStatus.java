package nav.portal.polling;

import nav.portal.core.enums.ServiceStatus;

import java.time.ZonedDateTime;

public class PolledServiceStatus {
    private String name;
    private ServiceStatus status;
    private String team;
    private ZonedDateTime timestamp;

    public PolledServiceStatus() {
    }

    public String getName() {
        return name;
    }

    public PolledServiceStatus setName(String name) {
        this.name = name;
        return this;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public PolledServiceStatus setStatus(ServiceStatus status) {
        this.status = status;
        return this;
    }

    public String getTeam() {
        return team;
    }

    public PolledServiceStatus setTeam(String team) {
        this.team = team;
        return this;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public PolledServiceStatus setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
