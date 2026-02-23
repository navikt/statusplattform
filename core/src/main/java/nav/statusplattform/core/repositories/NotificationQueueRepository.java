package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.NotificationQueueEntity;
import nav.statusplattform.core.enums.NotificationStatus;
import nav.statusplattform.core.enums.NotificationType;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextTable;
import org.fluentjdbc.DatabaseRow;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class NotificationQueueRepository {

    private final DbContextTable notificationQueueTable;

    public NotificationQueueRepository(DbContext dbContext) {
        notificationQueueTable = dbContext.table("notification_queue");
    }

    public UUID save(NotificationQueueEntity entity) {
        return notificationQueueTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("subscription_id", entity.getSubscriptionId())
                .setField("notification_type", entity.getNotificationType().getDbRepresentation())
                .setField("reference_id", entity.getReferenceId())
                .setField("subject", entity.getSubject())
                .setField("body_html", entity.getBodyHtml())
                .setField("status", entity.getStatus() != null ? entity.getStatus().getDbRepresentation() : NotificationStatus.PENDING.getDbRepresentation())
                .setField("attempts", entity.getAttempts())
                .execute()
                .getId();
    }

    public List<NotificationQueueEntity> findPending(int batchSize) {
        return notificationQueueTable
                .where("status", NotificationStatus.PENDING.getDbRepresentation())
                .whereExpression("attempts < ?", 3)
                .orderBy("created_at ASC")
                .limit(batchSize)
                .list(NotificationQueueRepository::toNotification);
    }

    public void markSent(UUID id) {
        notificationQueueTable.where("id", id)
                .update()
                .setField("status", NotificationStatus.SENT.getDbRepresentation())
                .setField("last_attempted", ZonedDateTime.now())
                .execute();
    }

    public void markFailed(UUID id) {
        notificationQueueTable.where("id", id)
                .update()
                .setField("status", NotificationStatus.FAILED.getDbRepresentation())
                .setFieldExpression("attempts", "attempts + 1")
                .setField("last_attempted", ZonedDateTime.now())
                .execute();
    }

    public void markPendingForRetry(UUID id) {
        notificationQueueTable.where("id", id)
                .update()
                .setField("status", NotificationStatus.PENDING.getDbRepresentation())
                .setFieldExpression("attempts", "attempts + 1")
                .setField("last_attempted", ZonedDateTime.now())
                .execute();
    }

    public boolean hasPendingNotification(UUID subscriptionId, UUID referenceId, String notificationType) {
        return notificationQueueTable
                .where("subscription_id", subscriptionId)
                .where("reference_id", referenceId)
                .where("notification_type", notificationType)
                .where("status", NotificationStatus.PENDING.getDbRepresentation())
                .getCount() > 0;
    }

    static NotificationQueueEntity toNotification(DatabaseRow row) {
        try {
            return new NotificationQueueEntity()
                    .setId(row.getUUID("id"))
                    .setSubscriptionId(row.getUUID("subscription_id"))
                    .setNotificationType(NotificationType.fromDb(row.getString("notification_type")).orElse(null))
                    .setReferenceId(row.getUUID("reference_id"))
                    .setSubject(row.getString("subject"))
                    .setBodyHtml(row.getString("body_html"))
                    .setStatus(NotificationStatus.fromDb(row.getString("status")).orElse(NotificationStatus.PENDING))
                    .setAttempts(row.getInt("attempts"))
                    .setLastAttempted(row.getZonedDateTime("last_attempted"))
                    .setCreatedAt(row.getZonedDateTime("created_at"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }
}
