package no.nav.statusplattform.jobs;

import nav.statusplattform.core.repositories.OtpRepository;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class OtpCleanupJob extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(OtpCleanupJob.class);

    private final DbContext dbContext;
    private DataSource dataSource;
    private final OtpRepository otpRepository;

    public OtpCleanupJob(DbContext dbContext) {
        this.dbContext = dbContext;
        this.otpRepository = new OtpRepository(dbContext);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run() {
        Thread.currentThread().setUncaughtExceptionHandler(new JobExceptionHandler(dbContext, dataSource));
        try {
            cleanup();
        } catch (Exception e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
    }

    private void cleanup() {
        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {
            logger.info("Starting OTP cleanup");
            otpRepository.cleanupExpired();
            otpRepository.cleanupOldRateLimits();
            logger.info("OTP cleanup completed");
        }
    }
}
