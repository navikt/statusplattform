package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.NotificationQueueEntity;
import nav.statusplattform.core.entities.OpsMessageEntity;
import nav.statusplattform.core.entities.SubscriptionEntity;
import nav.statusplattform.core.enums.NotificationStatus;
import nav.statusplattform.core.enums.NotificationType;
import nav.statusplattform.core.enums.ServiceStatus;
import nav.statusplattform.core.repositories.NotificationQueueRepository;
import nav.statusplattform.core.repositories.SubscriptionRepository;
import org.fluentjdbc.DbContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class NotificationQueueHelper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationQueueHelper.class);
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationQueueRepository notificationQueueRepository;

    public NotificationQueueHelper(DbContext dbContext) {
        this.subscriptionRepository = new SubscriptionRepository(dbContext);
        this.notificationQueueRepository = new NotificationQueueRepository(dbContext);
    }

    public void queueOpsMessageNotifications(OpsMessageEntity ops, List<UUID> serviceIds) {
        try {
            List<SubscriptionEntity> subscribers = subscriptionRepository.findSubscribersForServices(serviceIds);
            int queued = 0;

            for (SubscriptionEntity subscriber : subscribers) {
                if (notificationQueueRepository.hasPendingNotification(
                        subscriber.getId(), ops.getId(), NotificationType.OPS_MESSAGE.getDbRepresentation())) {
                    continue;
                }

                String header = ops.getExternalHeader() != null ? ops.getExternalHeader() : ops.getInternalHeader();
                String messageText = ops.getExternalText() != null ? ops.getExternalText() : ops.getInternalText();

                StringBuilder bodyHtml = new StringBuilder();
                bodyHtml.append("<h2>").append(escapeHtml(header)).append("</h2>");
                if (messageText != null) {
                    bodyHtml.append("<p>").append(escapeHtml(messageText)).append("</p>");
                }
                if (ops.getSeverity() != null) {
                    bodyHtml.append("<p>Alvorlighetsgrad: ").append(escapeHtml(ops.getSeverity().getDbRepresentation())).append("</p>");
                }
                String timeRange = formatTimeRange(ops);
                if (timeRange != null) {
                    bodyHtml.append("<p>Tidsperiode: ").append(escapeHtml(timeRange)).append("</p>");
                }

                NotificationQueueEntity notification = new NotificationQueueEntity()
                        .setSubscriptionId(subscriber.getId())
                        .setNotificationType(NotificationType.OPS_MESSAGE)
                        .setReferenceId(ops.getId())
                        .setSubject("Driftsmelding: " + header)
                        .setBodyHtml(bodyHtml.toString())
                        .setStatus(NotificationStatus.PENDING)
                        .setAttempts(0);

                notificationQueueRepository.save(notification);
                queued++;
            }

            logger.info("Queued {} ops message notifications for ops message {}", queued, ops.getId());
        } catch (Exception e) {
            logger.error("Failed to queue ops message notifications for ops message {}", ops.getId(), e);
        }
    }

    public void queueStatusChangeNotifications(UUID serviceId, String serviceName, ServiceStatus oldStatus, ServiceStatus newStatus) {
        try {
            List<SubscriptionEntity> subscribers = subscriptionRepository.findSubscribersForServices(List.of(serviceId));
            int queued = 0;

            for (SubscriptionEntity subscriber : subscribers) {
                if (notificationQueueRepository.hasPendingNotification(
                        subscriber.getId(), serviceId, NotificationType.STATUS_CHANGE.getDbRepresentation())) {
                    continue;
                }

                String bodyHtml = "<h2>Statusendring for " + escapeHtml(serviceName) + "</h2>"
                        + "<p>Status endret fra <strong>" + escapeHtml(oldStatus.getDbRepresentation())
                        + "</strong> til <strong>" + escapeHtml(newStatus.getDbRepresentation()) + "</strong></p>";

                NotificationQueueEntity notification = new NotificationQueueEntity()
                        .setSubscriptionId(subscriber.getId())
                        .setNotificationType(NotificationType.STATUS_CHANGE)
                        .setReferenceId(serviceId)
                        .setSubject("Statusendring: " + serviceName)
                        .setBodyHtml(bodyHtml)
                        .setStatus(NotificationStatus.PENDING)
                        .setAttempts(0);

                notificationQueueRepository.save(notification);
                queued++;
            }

            logger.info("Queued {} status change notifications for service {} ({} -> {})",
                    queued, serviceId, oldStatus, newStatus);
        } catch (Exception e) {
            logger.error("Failed to queue status change notifications for service {}", serviceId, e);
        }
    }

    private String formatTimeRange(OpsMessageEntity ops) {
        if (ops.getStartTime() == null) {
            return null;
        }
        String start = ops.getStartTime().format(DATETIME_FORMAT);
        if (ops.getEndTime() != null) {
            return start + " - " + ops.getEndTime().format(DATETIME_FORMAT);
        }
        return start;
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
