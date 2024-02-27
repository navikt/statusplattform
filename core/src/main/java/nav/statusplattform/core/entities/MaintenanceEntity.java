package nav.statusplattform.core.entities;

import java.time.ZonedDateTime;
import java.util.UUID;

public class MaintenanceEntity {

    private UUID id;
    private UUID serviceId;
    private String description;
    private ZonedDateTime start_time;
    private ZonedDateTime end_time;
    private ZonedDateTime created_at;

    public MaintenanceEntity() {
    }

    public UUID getId() {
        return id;
    }

    public MaintenanceEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public MaintenanceEntity setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MaintenanceEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public ZonedDateTime getStart_time() {
        return start_time;
    }

    public MaintenanceEntity setStart_time(ZonedDateTime start_time) {
        this.start_time = start_time;
        return this;
    }

    public ZonedDateTime getEnd_time() {
        return end_time;
    }

    public MaintenanceEntity setEnd_time(ZonedDateTime end_time) {
        this.end_time = end_time;
        return this;
    }

    public ZonedDateTime getCreated_at() {
        return created_at;
    }

    public MaintenanceEntity setCreated_at(ZonedDateTime created_at) {
        this.created_at = created_at;
        return this;
    }
}
