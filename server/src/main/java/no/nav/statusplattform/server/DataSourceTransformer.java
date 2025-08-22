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
        // Prefer NAIS injected full JDBC URL if present
        String jdbcUrl = System.getenv("DB_JDBC_URL");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            // Fallback build
            String host = config.hostname == null || config.hostname.isBlank()
                    ? System.getenv().getOrDefault("DB_HOST", "127.0.0.1")
                    : config.hostname;
            String port = String.valueOf(config.port == 0
                    ? Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "5432"))
                    : config.port);
            String db = config.dbName != null ? config.dbName : System.getenv("DB_NAME");
            jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            if (!jdbcUrl.contains("sslmode=")) {
                jdbcUrl += "?sslmode=require";
            }
        }

        String username = firstNonBlank(config.username, System.getenv("DB_USERNAME"));
        String password = firstNonBlank(config.password, System.getenv("DB_PASSWORD"));

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(32);
        // Do not fail fast while proxy starts
        hikari.setInitializationFailTimeout(-1);
        hikari.setConnectionTimeout(30_000);

        int maxTries = 30;              // ~60s total worst case
        Duration baseDelay = Duration.ofMillis(1000);
        HikariDataSource ds = null;
        for (int attempt = 1; attempt <= maxTries; attempt++) {
            try {
                logger.info("Creating HikariDataSource (attempt {}/{})", attempt, maxTries);
                ds = new HikariDataSource(hikari);
                break;
            } catch (Exception e) {
                if (attempt == maxTries) {
                    logger.error("Exhausted attempts creating DataSource", e);
                    throw e;
                }
                long sleepMs = baseDelay.toMillis() + (attempt * 200L);
                logger.warn("DB connect attempt {} failed: {}. Retrying in {} ms",
                        attempt, e.getMessage(), sleepMs);
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry DB connection", ie);
                }
            }
        }

        Flyway.configure()
                .dataSource(ds)
                .load()
                .migrate();

        return ds;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        return (b != null && !b.isBlank()) ? b : null;
    }
}