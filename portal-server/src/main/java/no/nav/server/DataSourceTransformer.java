package no.nav.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceTransformer {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceTransformer.class);
    public static DataSource create() {

        String dbUserName = System.getenv().getOrDefault("DB_USERNAME", "postgres");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        String dbHostname = System.getenv("DB_HOSTNAME");
        String dbPort = System.getenv().getOrDefault("DB_PORT", "5432");

        Properties properties = new Properties();
        properties.put("jdbcUrl", "jdbc:postgresql://" + dbHostname + ":" + dbPort + "/navstatus");
        properties.put("username", dbUserName);
        properties.put("password", password);
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
