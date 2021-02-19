package no.nav.brukergrupper.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public class DataSourceTransformer {
    public static DataSource create(Map<String, String> props) {
        Properties properties = new Properties();
        props.forEach(properties::put);
        return new HikariDataSource(new HikariConfig(properties));
    }
}
