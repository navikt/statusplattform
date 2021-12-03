package nav.portal.core.entities;


import nav.portal.core.enums.ServiceStatus;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class RecordEntity {

    private UUID id;
    private UUID serviceId;
    private ServiceStatus status;
    private ZonedDateTime created_at;
    private Integer responsetime;

    public RecordEntity() {

    }

    public UUID getId() {
        return id;
    }

    public RecordEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public RecordEntity setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public RecordEntity setStatus(ServiceStatus status) {
        this.status = status;
        return this;
    }

    public ZonedDateTime getCreated_at() {
        return created_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordEntity that = (RecordEntity) o;
        return id.equals(that.id) && serviceId.equals(that.serviceId) && status == that.status && created_at.equals(that.created_at) && responsetime.equals(that.responsetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceId, status, created_at, responsetime);
    }

    public RecordEntity setCreated_at(ZonedDateTime created_at) {
        this.created_at = created_at;
        return this;
    }

    public Integer getResponsetime() {
        return responsetime;
    }

    public RecordEntity setResponsetime(Integer responsetime) {
        this.responsetime = responsetime;
        return this;
    }
}
