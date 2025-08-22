// server/src/main/java/no/nav/statusplattform/server/DataSourceTransformer.java
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

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(32);
        hikari.setInitializationFailTimeout(-1);
        hikari.setConnectionTimeout(30_000);

        int maxTries = 30;
        Duration baseDelay = Duration.ofSeconds(1);
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
                long sleepMs = baseDelay.toMillis() + attempt * 200L;
                logger.warn("DB connect attempt {} failed: {}. Retrying in {} ms", attempt, e.getMessage(), sleepMs);
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry DB connection", ie);
                }
            }
        }

        Flyway.configure().dataSource(ds).load().migrate();
        return ds;
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}