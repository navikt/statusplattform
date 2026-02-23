package nav.statusplattform.core.entities;

import nav.statusplattform.core.enums.NotificationStatus;
import nav.statusplattform.core.enums.NotificationType;

import java.time.ZonedDateTime;
import java.util.UUID;

public class NotificationQueueEntity {

    private UUID id;
    private UUID subscriptionId;
    private NotificationType notificationType;
    private UUID referenceId;
    private String subject;
    private String bodyHtml;
    private NotificationStatus status;
    private int attempts;
    private ZonedDateTime lastAttempted;
    private ZonedDateTime createdAt;

    public NotificationQueueEntity() {
    }

    public UUID getId() {
        return id;
    }

    public NotificationQueueEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public NotificationQueueEntity setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public NotificationQueueEntity setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public NotificationQueueEntity setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public NotificationQueueEntity setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public NotificationQueueEntity setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
        return this;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public NotificationQueueEntity setStatus(NotificationStatus status) {
        this.status = status;
        return this;
    }

    public int getAttempts() {
        return attempts;
    }

    public NotificationQueueEntity setAttempts(int attempts) {
        this.attempts = attempts;
        return this;
    }

    public ZonedDateTime getLastAttempted() {
        return lastAttempted;
    }

    public NotificationQueueEntity setLastAttempted(ZonedDateTime lastAttempted) {
        this.lastAttempted = lastAttempted;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public NotificationQueueEntity setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
