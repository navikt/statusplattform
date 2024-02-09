package no.nav.statusplattform.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.statusplattform.AppConfig.DbConfig;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceTransformer {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceTransformer.class);
    public static DataSource create(DbConfig config) {
        Properties properties = new Properties();
        properties.put("jdbcUrl", "jdbc:postgresql://" + config.hostname + ":" + config.port + "/" + config.dbName);
        properties.put("username", config.username);
        properties.put("password", config.password);
        properties.put("maximumPoolSize","32");

        int count = 0;
        int maxTries = 10;
        HikariDataSource dataSource;
        while(true) {
            try {
                dataSource = new HikariDataSource(new HikariConfig(properties));
                break;
            } catch (Exception e) {
                if (++count == maxTries) {
                    throw e;
                }
            }
        }
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }
}
