package nav.statusplattform.core.entities;

import nav.statusplattform.core.enums.ServiceStatus;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class RecordDeltaEntity {
    private UUID id;
    private UUID serviceId;
    private ServiceStatus status;
    private ZonedDateTime created_at;
    private Integer counter;
    private Boolean active;
    private ZonedDateTime updated_at;



    public RecordDeltaEntity() {
    }

    public ZonedDateTime getUpdated_at() {
        return updated_at;
    }

    public RecordDeltaEntity setUpdated_at(ZonedDateTime updated_at) {
        this.updated_at = updated_at;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    public RecordDeltaEntity setActive(Boolean active) {
        this.active = active;
        return this;
    }
    public UUID getId() {
        return id;
    }


    public RecordDeltaEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public RecordDeltaEntity setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public RecordDeltaEntity setStatus(ServiceStatus status) {
        this.status = status;
        return this;
    }

    public ZonedDateTime getCreated_at() {
        return created_at;
    }

    public RecordDeltaEntity setCreated_at(ZonedDateTime created_at) {
        this.created_at = created_at;
        return this;
    }

    public Integer getCounter() {
        return counter;
    }

    public RecordDeltaEntity setCounter(Integer counter) {
        this.counter = counter;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordDeltaEntity that = (RecordDeltaEntity) o;
        return id.equals(that.id) && serviceId.equals(that.serviceId) && status == that.status && created_at.equals(that.created_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceId, status, created_at);
    }






}
