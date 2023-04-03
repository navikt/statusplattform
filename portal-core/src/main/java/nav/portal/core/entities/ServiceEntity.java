package nav.portal.core.entities;

import nav.portal.core.enums.ServiceType;

import java.util.Objects;
import java.util.UUID;

public class ServiceEntity {
    private String name;
    private UUID id;
    private ServiceType type;
    private String team;
    private String monitorlink;
    private String polling_url;
    private Boolean pollingOnPrem;
    private Boolean isDeleted;
    private Boolean statusNotFromTeam;

    public ServiceEntity() {
    }


    public String getName() {
        return name;
    }

    public ServiceEntity setName(String name) {
        this.name = name;
        return this;
    }

    public UUID getId() {
        return id;
    }
    public Boolean getDeleted() {
        return isDeleted;
    }

    public ServiceEntity setDeleted(Boolean deleted) {
        isDeleted = deleted;
        return this;
    }

    public ServiceEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public Boolean getStatusNotFromTeam() {
        return statusNotFromTeam;
    }

    public ServiceEntity setStatusNotFromTeam(Boolean statusNotFromTeam) {
        this.statusNotFromTeam = statusNotFromTeam;
        return this;
    }

    public ServiceType getType() {
        return type;
    }

    public ServiceEntity setType(ServiceType type) {
        this.type = type;
        return this;
    }

    public String getTeam() {
        return team;
    }

    public ServiceEntity setTeam(String team) {
        this.team = team;
        return this;
    }

    public String getMonitorlink() {
        return monitorlink;
    }

    public ServiceEntity setMonitorlink(String monitorlink) {
        this.monitorlink = monitorlink;
        return this;
    }


    public String getPolling_url() {
        return polling_url;
    }

    public ServiceEntity setPolling_url(String polling_url) {
        this.polling_url = polling_url;
        return this;
    }

    public Boolean getPollingOnPrem() {
        return pollingOnPrem;
    }

    public ServiceEntity setPollingOnPrem(Boolean pollingOnPrem) {
        this.pollingOnPrem = pollingOnPrem;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceEntity that = (ServiceEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(team, that.team) && Objects.equals(monitorlink, that.monitorlink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, type, team, monitorlink);
    }
}
