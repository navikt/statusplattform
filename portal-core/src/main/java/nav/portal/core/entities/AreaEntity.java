package nav.portal.core.entities;


import java.util.Objects;
import java.util.UUID;

public class AreaEntity {

    private UUID id;
    private String name;
    private String description;
    private String icon;


    public AreaEntity() {
    }

    //TODO Lage enum av icon:
    public AreaEntity(UUID id, String name, String description, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public AreaEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public AreaEntity setName(String name) {
        this.name = name;
        return this;
    }

    public AreaEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public AreaEntity setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaEntity that = (AreaEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, icon);
    }
}
