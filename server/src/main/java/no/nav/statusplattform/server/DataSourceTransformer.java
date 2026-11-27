package no.nav.statusplattform.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.statusplattform.AppConfig.DbConfig;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.Duration;

public class DataSourceTransformer {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceTransformer.class);

    public static DataSource create(DbConfig config) {
        String jdbcUrl = System.getenv("DB_JDBC_URL");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            String host = blank(config.hostname) ? System.getenv().getOrDefault("DB_HOST", "127.0.0.1") : config.hostname;
            String port = config.port == 0 ? System.getenv().getOrDefault("DB_PORT", "5432") : String.valueOf(config.port);
            String db = firstNonBlank(config.dbName,
                    System.getenv("DB_NAME"),
                    System.getenv("DB_DATABASE")); // NAIS provides DB_DATABASE
            jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            if (!jdbcUrl.contains("sslmode=")) {
                jdbcUrl += "?sslmode=require";
            }
        }

        String username = firstNonBlank(config.username, System.getenv("DB_USERNAME"));
        String password = firstNonBlank(config.password, System.getenv("DB_PASSWORD"));

        // Migrate with a dedicated, short-lived pool so migrations never compete with the
        // application pool for connections, and so the two connections Flyway needs are
        // released again as soon as the migration is done.
        migrate(jdbcUrl, username, password);

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("statusplattform-app");
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(maxPoolSize());
        hikari.setMinimumIdle(minimumIdle());
        hikari.setInitializationFailTimeout(-1);
        hikari.setConnectionTimeout(30_000);
        hikari.setIdleTimeout(Duration.ofMinutes(5).toMillis());
        hikari.setKeepaliveTime(Duration.ofMinutes(2).toMillis());
        hikari.setMaxLifetime(Duration.ofMinutes(20).toMillis());
        hikari.setLeakDetectionThreshold(Duration.ofSeconds(60).toMillis());

        logger.info("Creating application HikariDataSource with maximumPoolSize={}, minimumIdle={}",
                hikari.getMaximumPoolSize(), hikari.getMinimumIdle());
        return new HikariDataSource(hikari);
    }

    private static void migrate(String jdbcUrl, String username, String password) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("statusplattform-flyway");
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        // Flyway needs two connections: one for the schema history table and one for migrating.
        hikari.setMaximumPoolSize(2);
        hikari.setMinimumIdle(0);
        hikari.setInitializationFailTimeout(-1);
        hikari.setConnectionTimeout(10_000);

        int maxTries = 30;
        try (HikariDataSource migrationDataSource = new HikariDataSource(hikari)) {
            for (int attempt = 1; attempt <= maxTries; attempt++) {
                try {
                    logger.info("Running Flyway migration (attempt {}/{})", attempt, maxTries);
                    Flyway.configure().dataSource(migrationDataSource).load().migrate();
                    return;
                } catch (RuntimeException e) {
                    if (attempt == maxTries) {
                        logger.error("Exhausted attempts running Flyway migration", e);
                        throw e;
                    }
                    long sleepMs = Duration.ofSeconds(1).toMillis() + attempt * 500L;
                    logger.warn("Migration attempt {} failed: {}. Retrying in {} ms", attempt, e.getMessage(), sleepMs);
                    sleep(sleepMs);
                }
            }
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to retry database migration", ie);
        }
    }

    private static int maxPoolSize() {
        return intFromEnv("DB_MAX_POOL_SIZE", 10);
    }

    private static int minimumIdle() {
        return Math.min(intFromEnv("DB_MIN_IDLE", 2), maxPoolSize());
    }

    private static int intFromEnv(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid value '{}' for {}, using default {}", value, name, defaultValue);
            return defaultValue;
        }
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}