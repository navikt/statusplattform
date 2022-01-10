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
    private String description;
    private String logglink;
    private String polling_url;

    public ServiceEntity() {
    }

    public ServiceEntity(String name, UUID id, ServiceType type, String team, String monitorlink, String description, String logglink, String polling_url) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.team = team;
        this.monitorlink = monitorlink;
        this.description = description;
        this.logglink = logglink;
        this.polling_url = polling_url;
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

    public ServiceEntity setId(UUID id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public ServiceEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLogglink() {
        return logglink;
    }

    public ServiceEntity setLogglink(String logglink) {
        this.logglink = logglink;
        return this;
    }
    public String getPolling_url() {
        return polling_url;
    }

    public ServiceEntity setPolling_url(String polling_url) {
        this.polling_url = polling_url;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceEntity that = (ServiceEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(team, that.team) && Objects.equals(monitorlink, that.monitorlink) && Objects.equals(description, that.description) && Objects.equals(logglink, that.logglink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, type, team, monitorlink, description, logglink);
    }
}
