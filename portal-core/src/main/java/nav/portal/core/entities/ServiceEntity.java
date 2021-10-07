package nav.portal.core.entities;

import nav.portal.core.enums.ServiceType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ServiceEntity {
    private String name;
    private UUID id;
    private ServiceType type;
    private String team;
    private List<ServiceEntity> dependencies;
    private String monitorlink;
    private String description;
    private String logglink;

    public  ServiceEntity() {
    }

    public ServiceEntity(String name, UUID id, ServiceType type, String team, String monitorlink, String description, String logglink) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.team = team;
        this.monitorlink = monitorlink;
        this.description = description;
        this.logglink = logglink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }
    public List<ServiceEntity> getDependencies() {
        return dependencies;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getMonitorlink() {
        return monitorlink;
    }

    public void setMonitorlink(String monitorlink) {
        this.monitorlink = monitorlink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogglink() {
        return logglink;
    }

    public void setLogglink(String logglink) {
        this.logglink = logglink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceEntity that = (ServiceEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(team, that.team) && Objects.equals(dependencies, that.dependencies) && Objects.equals(monitorlink, that.monitorlink) && Objects.equals(description, that.description) && Objects.equals(logglink, that.logglink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, type, team, dependencies, monitorlink, description, logglink);
    }
}
