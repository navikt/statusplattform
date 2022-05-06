package no.nav.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.portal.oauth2.AuthenticationFilter;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataSourceTransformer {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    public static DataSource create() {

        logger.info("DatasourceTransformer: ");
        Properties properties = new Properties();
        String userName = "navstatus";//System.getenv("DB_USERNAME");
        properties.put("jdbcUrl", "jdbc:postgresql://127.0.0.1:5432/navstatus");
        properties.put("username", userName);

        String password ="X-jPWBcaZpVnMzD8glJtNw7t9Kwxrq63TqadCifPbuY";// System.getenv("DB_PASSWORD");
        logger.info("DB_PASSWORD Ecoded: "+ password);
        properties.put("password", password);
        logger.info("DB_USERNAME: "+ properties.get("username"));
        logger.info("DB_PASSWORD: "+ properties.get("password"));
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
