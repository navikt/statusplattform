package nav.portal.core.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DashboardEntity {

    private UUID id;
    private String name;
    private List<AreaEntity> areas;

    public DashboardEntity() {
    }

    public String getName() {
        return name;
    }

    public DashboardEntity setName(String name) {
        this.name = name;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public DashboardEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public List<AreaEntity> getAreas() {
        return areas;
    }


    public DashboardEntity setAreas(List<AreaEntity> areas) {
        this.areas = areas;
        return this;
    }
}


