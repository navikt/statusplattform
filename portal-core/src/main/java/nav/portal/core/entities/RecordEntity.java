package nav.portal.core.entities;


import nav.portal.core.enums.ServiceStatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RecordEntity {
    private UUID serviceId;
    private ServiceStatus status;
    private ZonedDateTime created_at;
    private Integer responsetime;



    public RecordEntity(UUID serviceId, ServiceStatus status, ZonedDateTime created_at, Integer responsetime) {
        this.serviceId = serviceId;
        this.status = status;
        this.responsetime = responsetime;
        this.created_at = created_at;
    }

    public UUID getServiceId() {
        return serviceId;
    }


    public ServiceStatus getStatus() {
        return status;
    }


    public ZonedDateTime getCreated_at() {
        return created_at;
    }

    public Integer getResponsetime() {
        return responsetime;
    }


}
