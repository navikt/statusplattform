package no.nav.statusplattform;

import java.util.Optional;

public class AppConfig {

    public static class DbConfig {
        public final String hostname = getEnvVar("DB_HOSTNAME").orElseThrow(IllegalStateException::new);
        public final Integer port = getEnvVar("DB_PORT").map(Integer::parseInt).orElse(5432);
        public final String username = getEnvVar("DB_USERNAME").orElse("postgres");
        public final String password = getEnvVar("DB_PASSWORD").orElse("");
        public final String dbName = getEnvVar("DB_NAME").orElse("navstatus");
    }

    public static class AzureConfig {
        public final String oath2Scope = getEnvVar("ENV").orElseThrow(IllegalStateException::new);
        public final String tenant = getEnvVar("TENANT").orElseThrow(IllegalStateException::new);
        public final String clientId = getEnvVar("AZURE_APP_CLIENT_ID").orElseThrow(IllegalStateException::new);
        public final String clientSecret = getEnvVar("AZURE_APP_CLIENT_SECRET").orElseThrow(IllegalStateException::new);
    }

    public AppConfig(DbConfig dbConfig, AzureConfig azConfig) {
        this.dbConfig = dbConfig;
        this.azConfig = azConfig;
    }

    public static Optional<String> getEnvVar(String envVarName) {
        return Optional.ofNullable(System.getenv(envVarName));
    }

    public final DbConfig dbConfig;
    public final AzureConfig azConfig;
    public final String teamkatalogApi = System.getenv("teamkatalogApiUrl");
    public final String frontendCorsValue = System.getenv("FRONTEND_LOCATION");
    public final String swaggerApiKey = System.getenv("swagger-api-key");
    public final String host = Optional.ofNullable(System.getenv("HOST")).orElse("localhost");
    public final int port = Optional.ofNullable(System.getenv("HTTP_PLATFORM_PORT")).map(Integer::parseInt).orElse(3005);
}
