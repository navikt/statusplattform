package nav.portal.core.entities;

import java.util.List;
import java.util.Objects;

public class SubAreaWithServices {
    private SubAreaEntity subArea;
    private List<ServiceEntity> services;


    public SubAreaWithServices(SubAreaEntity subArea, List<ServiceEntity> services) {
        this.subArea = subArea;
        this.services = services;
    }

    public SubAreaEntity getSubArea() {
        return subArea;
    }

    public void setSubArea(SubAreaEntity subArea) {
        this.subArea = subArea;
    }

    public List<ServiceEntity> getServices() {
        return services;
    }

    public void setServices(List<ServiceEntity> services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubAreaWithServices)) return false;
        SubAreaWithServices that = (SubAreaWithServices) o;
        return Objects.equals(getSubArea(), that.getSubArea()) && Objects.equals(getServices(), that.getServices());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubArea(), getServices());
    }
}


