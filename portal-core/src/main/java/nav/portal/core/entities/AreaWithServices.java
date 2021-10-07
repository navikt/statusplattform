package nav.portal.core.entities;

import java.util.List;
import java.util.Objects;

public class AreaWithServices {
    private AreaEntity area;
    private List<ServiceEntity> services;

    public AreaWithServices(AreaEntity area, List<ServiceEntity> services) {
        this.area = area;
        this.services = services;
    }

    public List<ServiceEntity> getServices() {
        return services;
    }

    public AreaEntity getArea() {
        return area;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AreaWithServices that = (AreaWithServices) o;
        return Objects.equals(area, that.area) && Objects.equals(services, that.services);
    }

    @Override
    public int hashCode() {
        return Objects.hash(area, services);
    }
}
