package no.nav.statusplattform.jobs;

import org.fluentjdbc.DbContext;

import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobHandler {

    private final DbContext dbContext = new DbContext();
    private OpeningHoursDailyEval openingHoursDailyEval;
    private NotificationSenderJob notificationSenderJob;
    private OtpCleanupJob otpCleanupJob;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final int UPDATE_TIME = 24;
    private final int NOTIFICATION_INTERVAL_SECONDS = 30;

    public JobHandler() {
        openingHoursDailyEval = new OpeningHoursDailyEval(dbContext);
        openingHoursDailyEval.setDaemon(true);
        openingHoursDailyEval.setName("Polling thread");

        notificationSenderJob = new NotificationSenderJob(dbContext);
        notificationSenderJob.setDaemon(true);
        notificationSenderJob.setName("Notification sender thread");

        otpCleanupJob = new OtpCleanupJob(dbContext);
        otpCleanupJob.setDaemon(true);
        otpCleanupJob.setName("OTP cleanup thread");
    }

    public void setDataSource(DataSource dataSource){
        openingHoursDailyEval.setDataSource(dataSource);
        notificationSenderJob.setDataSource(dataSource);
        otpCleanupJob.setDataSource(dataSource);
    }

    public void setEmailSender(NotificationSenderJob.EmailSender emailSender) {
        notificationSenderJob.setEmailSender(emailSender);
    }

    public void start(){
        scheduler.scheduleWithFixedDelay(openingHoursDailyEval, 0, UPDATE_TIME, TimeUnit.HOURS);
        scheduler.scheduleWithFixedDelay(notificationSenderJob, 30, NOTIFICATION_INTERVAL_SECONDS, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(otpCleanupJob, 0, UPDATE_TIME, TimeUnit.HOURS);
    }
}
