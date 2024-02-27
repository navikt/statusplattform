package nav.statusplattform.core.entities;


import java.util.Objects;
import java.util.UUID;

public class SubAreaEntity {

    private UUID id;
    private String name;


    public SubAreaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public SubAreaEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SubAreaEntity setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubAreaEntity that = (SubAreaEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}