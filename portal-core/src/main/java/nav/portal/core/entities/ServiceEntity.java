package nav.portal.core.entities;

import java.util.List;

public class ServiceEntity {
    private String name;
    private String id;
    private String type;
    private String team;
    private List<String> dependencies;
    private String monitorlink;
    private String description;
    private String logglink;

    public ServiceEntity() {
    }

    public ServiceEntity(String name, String id, String type, String team, List<String> dependencies, String monitorlink, String description, String logglink) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.team = team;
        this.dependencies = dependencies;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
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
}
