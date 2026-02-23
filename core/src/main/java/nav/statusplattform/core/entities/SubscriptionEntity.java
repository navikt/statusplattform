package nav.statusplattform.core.entities;

import java.time.ZonedDateTime;
import java.util.UUID;

public class SubscriptionEntity {

    private UUID id;
    private String email;
    private boolean emailVerified;
    private UUID unsubscribeToken;
    private boolean isInternal;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public SubscriptionEntity() {
    }

    public UUID getId() {
        return id;
    }

    public SubscriptionEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public SubscriptionEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public SubscriptionEntity setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        return this;
    }

    public UUID getUnsubscribeToken() {
        return unsubscribeToken;
    }

    public SubscriptionEntity setUnsubscribeToken(UUID unsubscribeToken) {
        this.unsubscribeToken = unsubscribeToken;
        return this;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public SubscriptionEntity setInternal(boolean internal) {
        isInternal = internal;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public SubscriptionEntity setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public SubscriptionEntity setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
