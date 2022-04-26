package no.nav.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataSourceTransformer {
    public static DataSource create(Map<String, String> props) {


        Properties properties = new Properties();
        props.forEach(properties::put);
        String passwordName = properties.get("passwordName").toString();
        properties.remove("passwordName");
        properties.put("password", System.getenv(passwordName));

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
