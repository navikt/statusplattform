package nav.portal.core.entities;

import java.util.List;
import java.util.Objects;

public class AreaWithServices {
    private AreaEntity area;
    private List<ServiceEntity> services;
    private List<SubAreaEntity> subAreas;


    public AreaWithServices(AreaEntity area, List<ServiceEntity> services, List<SubAreaEntity> subAreas) {
        this.area = area;
        this.services = services;
        this.subAreas = subAreas;
    }
    public List<SubAreaEntity> getSubAreas() {
        return subAreas;
    }

    public AreaWithServices setSubAreas(List<SubAreaEntity> subAreas) {
        this.subAreas = subAreas;
        return this;
    }

    public AreaWithServices addSubArea(SubAreaEntity subArea) {
        this.subAreas.add(subArea);
        return this;
    }

    public AreaWithServices addService(ServiceEntity serviceEntity) {
        this.services.add(serviceEntity);
        return this;
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
