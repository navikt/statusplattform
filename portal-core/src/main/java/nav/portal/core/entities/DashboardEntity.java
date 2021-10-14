package nav.portal.core.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DashboardEntity {

    private UUID id;
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DashboardEntity that = (DashboardEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}


