package no.nav.statusplattform.jobs;

import nav.statusplattform.core.entities.NotificationQueueEntity;
import nav.statusplattform.core.entities.SubscriptionEntity;
import nav.statusplattform.core.repositories.NotificationQueueRepository;
import nav.statusplattform.core.repositories.SubscriptionRepository;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class NotificationSenderJob extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSenderJob.class);
    private static final int BATCH_SIZE = 50;

    private final DbContext dbContext;
    private DataSource dataSource;
    private final NotificationQueueRepository notificationQueueRepository;
    private final SubscriptionRepository subscriptionRepository;
    private EmailSender emailSender;

    public NotificationSenderJob(DbContext dbContext) {
        this.dbContext = dbContext;
        this.notificationQueueRepository = new NotificationQueueRepository(dbContext);
        this.subscriptionRepository = new SubscriptionRepository(dbContext);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler(new JobExceptionHandler(dbContext, dataSource));
        try {
            processNotifications();
        } catch (Exception e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
    }

    private void processNotifications() {
        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {
            List<NotificationQueueEntity> pending = notificationQueueRepository.findPending(BATCH_SIZE);
            if (pending.isEmpty()) {
                return;
            }
            logger.info("Processing {} pending notifications", pending.size());

            for (NotificationQueueEntity notification : pending) {
                try {
                    Optional<SubscriptionEntity> subscriber = subscriptionRepository.retrieve(notification.getSubscriptionId());
                    if (subscriber.isEmpty()) {
                        notificationQueueRepository.markSent(notification.getId());
                        continue;
                    }

                    String unsubscribeToken = subscriber.get().getUnsubscribeToken() != null
                            ? subscriber.get().getUnsubscribeToken().toString() : "";
                    String bodyWithUnsubscribe = notification.getBodyHtml()
                            + "<hr style='margin-top:2rem;border:none;border-top:1px solid #e1e5e9;'>"
                            + "<p style='font-size:0.8rem;color:#5e6c84;'>"
                            + "<a href='{{STATUS_PAGE_URL}}/avmeld/" + unsubscribeToken + "'>Avmeld statusoppdateringer</a>"
                            + "</p>";

                    emailSender.sendNotificationEmail(
                            subscriber.get().getEmail(),
                            notification.getSubject(),
                            bodyWithUnsubscribe
                    );
                    notificationQueueRepository.markSent(notification.getId());
                    logger.debug("Sent notification {} to {}", notification.getId(), subscriber.get().getEmail());

                } catch (Exception e) {
                    logger.error("Failed to send notification {}", notification.getId(), e);
                    if (notification.getAttempts() + 1 >= 3) {
                        notificationQueueRepository.markFailed(notification.getId());
                    } else {
                        notificationQueueRepository.markPendingForRetry(notification.getId());
                    }
                }
            }
        }
    }

    /**
     * Interface for email sending to decouple from the api module's EmailService.
     */
    public interface EmailSender {
        void sendNotificationEmail(String email, String subject, String bodyHtml);
    }
}
