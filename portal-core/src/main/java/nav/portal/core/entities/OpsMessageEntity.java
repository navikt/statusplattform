package nav.portal.core.entities;

import java.util.Objects;
import java.util.UUID;

public class OpsMessageEntity {

    private UUID id;
    private String internalHeader;
    private String internalText;
    private String externalHeader;
    private String externalText;
    private boolean isActive;
    private boolean onlyShowForNavEmployees;
    private boolean deleted;

    public boolean isDeleted() {
        return deleted;
    }

    public OpsMessageEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public OpsMessageEntity() {
    }

    public boolean getOnlyShowForNavEmployees() {
        return onlyShowForNavEmployees;
    }

    public OpsMessageEntity setOnlyShowForNavEmployees(boolean onlyShowForNavEmployees) {
        this.onlyShowForNavEmployees = onlyShowForNavEmployees;
        return this;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public OpsMessageEntity setIsActive(boolean active) {
        isActive = active;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public OpsMessageEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getInternalHeader() {
        return internalHeader;
    }

    public OpsMessageEntity setInternalHeader(String internalHeader) {
        this.internalHeader = internalHeader;
        return this;
    }

    public String getInternalText() {
        return internalText;
    }

    public OpsMessageEntity setInternalText(String internalText) {
        this.internalText = internalText;
        return this;
    }

    public String getExternalHeader() {
        return externalHeader;
    }

    public OpsMessageEntity setExternalHeader(String externalHeader) {
        this.externalHeader = externalHeader;
        return this;
    }

    public String getExternalText() {
        return externalText;
    }

    public OpsMessageEntity setExternalText(String externalText) {
        this.externalText = externalText;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpsMessageEntity that = (OpsMessageEntity) o;
        return isActive == that.isActive && onlyShowForNavEmployees == that.onlyShowForNavEmployees && Objects.equals(id, that.id) && Objects.equals(internalHeader, that.internalHeader) && Objects.equals(internalText, that.internalText) && Objects.equals(externalHeader, that.externalHeader) && Objects.equals(externalText, that.externalText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, internalHeader, internalText, externalHeader, externalText, isActive, onlyShowForNavEmployees);
    }
}
