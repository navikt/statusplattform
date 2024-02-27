package nav.statusplattform.core.entities;


import java.util.Objects;
import java.util.UUID;

public class AreaEntity {

    private UUID id;
    private String name;
    private String description;
    private Boolean contains_components;


    public AreaEntity() {
    }

    //TODO Lage enum av icon:
    public AreaEntity(UUID id, String name, String description, Boolean contains_components) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.contains_components = contains_components;
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


    public Boolean getContains_components() {
        return contains_components;
    }

    public AreaEntity setContains_components(Boolean contains_components) {
        this.contains_components = contains_components;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaEntity that = (AreaEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(contains_components, that.contains_components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, contains_components);
    }
}
