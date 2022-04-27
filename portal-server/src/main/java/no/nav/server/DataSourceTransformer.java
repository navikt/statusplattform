package no.nav.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataSourceTransformer {
    public static DataSource create() {

        Properties properties = new Properties();
        properties.put("jdbcUrl", "jdbc:postgresql://127.0.0.1:5432/statusdb");
        properties.put("username", "postgres");
        properties.put("password", System.getenv("dbpass"));
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
                };
            }
        }



        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }



}
