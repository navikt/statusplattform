package nav.portal.core.entities;

import net.sourceforge.jtds.jdbc.DateTime;

import java.sql.Timestamp;

public class RecordEntity {
    private String serviceId;
    private String status; //Skal være enum, enumet "bor" ikke her. Bør man ha eget enum for core?
    private Timestamp timestamp;
    private Integer responsetime;

    public RecordEntity() {
    }

    public RecordEntity(String serviceId, String status, Timestamp timestamp, Integer responsetime) {
        this.serviceId = serviceId;
        this.status = status;
        this.timestamp = timestamp;
        this.responsetime = responsetime;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getResponsetime() {
        return responsetime;
    }

    public void setResponsetime(Integer responsetime) {
        this.responsetime = responsetime;
    }
}
