package nav.portal.core.entities;


import nav.portal.core.enums.RecordSource;
import nav.portal.core.enums.ServiceStatus;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class RecordEntity {

    private UUID id;
    private UUID serviceId;
    private ServiceStatus status;
    private String description;
    private String logglink;
    private ZonedDateTime created_at;
    private Integer responsetime;
    private Integer counter;
    private Boolean active;
    private RecordSource recordSource;



    public RecordEntity() {
    }

    public Boolean getActive() {
        return active;
    }

    public RecordEntity setActive(Boolean active) {
        this.active = active;
        return this;
    }
    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getLogglink() {
        return logglink;
    }

    public RecordEntity setLogglink(String logglink) {
        this.logglink = logglink;
        return this;
    }

    public RecordEntity setDescription(String description) {
        this.description = description;
        return this;
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

    public Integer getCounter() {
        return counter;
    }

    public RecordEntity setCounter(Integer counter) {
        this.counter = counter;
        return this;
    }

    public RecordSource getRecordSource() {
        return recordSource;
    }

    public RecordEntity setRecordSource(RecordSource recordSource) {
        this.recordSource = recordSource;
        return this;
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
