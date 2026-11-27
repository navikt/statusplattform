package nav.statusplattform.core.repositories;

import nav.statusplattform.core.entities.SubscriptionEntity;
import nav.statusplattform.core.exceptionHandling.ExceptionUtil;
import org.fluentjdbc.*;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;

public class SubscriptionRepository {

    private final DbContextTable subscriptionTable;
    private final DbContextTable subscriptionServiceTable;

    public SubscriptionRepository(DbContext dbContext) {
        subscriptionTable = dbContext.table("subscription");
        subscriptionServiceTable = dbContext.table("subscription_service");
    }

    public UUID save(SubscriptionEntity entity) {
        if (entity.getUnsubscribeToken() == null) {
            entity.setUnsubscribeToken(UUID.randomUUID());
        }
        DatabaseSaveResult<UUID> result = subscriptionTable.newSaveBuilderWithUUID("id", entity.getId())
                .setField("email", entity.getEmail())
                .setField("email_verified", entity.isEmailVerified())
                .setField("unsubscribe_token", entity.getUnsubscribeToken())
                .setField("is_internal", entity.isInternal())
                .execute();
        return result.getId();
    }

    public void setServicesOnSubscription(UUID subscriptionId, List<UUID> serviceIds) {
        subscriptionServiceTable.where("subscription_id", subscriptionId).executeDelete();
        if (serviceIds == null || serviceIds.isEmpty()) {
            return;
        }
        for (UUID serviceId : serviceIds) {
            subscriptionServiceTable.insert()
                    .setField("subscription_id", subscriptionId)
                    .setField("service_id", serviceId)
                    .execute();
        }
    }

    public List<UUID> getServiceIdsForSubscription(UUID subscriptionId) {
        return subscriptionServiceTable.where("subscription_id", subscriptionId)
                .list(row -> {
                    try {
                        return row.getUUID("service_id");
                    } catch (SQLException e) {
                        throw ExceptionUtil.soften(e);
                    }
                });
    }

    public Optional<SubscriptionEntity> findByEmail(String email) {
        return subscriptionTable.where("email", email)
                .singleObject(SubscriptionRepository::toSubscription);
    }

    public Optional<SubscriptionEntity> findByUnsubscribeToken(UUID token) {
        return subscriptionTable.where("unsubscribe_token", token)
                .singleObject(SubscriptionRepository::toSubscription);
    }

    public Optional<SubscriptionEntity> retrieve(UUID id) {
        return subscriptionTable.where("id", id)
                .singleObject(SubscriptionRepository::toSubscription);
    }

    public List<SubscriptionEntity> findSubscribersForServices(List<UUID> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        DbContextTableAlias sub = subscriptionTable.alias("sub");
        DbContextTableAlias ss = subscriptionServiceTable.alias("ss");

        Set<UUID> seen = new HashSet<>();
        List<SubscriptionEntity> result = new ArrayList<>();

        sub.where("email_verified", true)
                .leftJoin(sub.column("id"), ss.column("subscription_id"))
                .whereIn(ss.column("service_id"), serviceIds)
                .list(row -> {
                    SubscriptionEntity entity = toSubscription(row.table(sub));
                    if (seen.add(entity.getId())) {
                        result.add(entity);
                    }
                    return null;
                });
        return result;
    }

    public void markEmailVerified(String email) {
        subscriptionTable.where("email", email)
                .update()
                .setField("email_verified", true)
                .setField("updated_at", ZonedDateTime.now())
                .execute();
    }

    public void delete(UUID id) {
        subscriptionTable.where("id", id).executeDelete();
    }

    public void deleteByUnsubscribeToken(UUID token) {
        subscriptionTable.where("unsubscribe_token", token).executeDelete();
    }

    static SubscriptionEntity toSubscription(DatabaseRow row) {
        try {
            return new SubscriptionEntity()
                    .setId(row.getUUID("id"))
                    .setEmail(row.getString("email"))
                    .setEmailVerified(row.getBoolean("email_verified"))
                    .setUnsubscribeToken(row.getUUID("unsubscribe_token"))
                    .setInternal(row.getBoolean("is_internal"))
                    .setCreatedAt(row.getZonedDateTime("created_at"))
                    .setUpdatedAt(row.getZonedDateTime("updated_at"));
        } catch (SQLException e) {
            throw ExceptionUtil.soften(e);
        }
    }
}
