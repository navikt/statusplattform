package no.nav.statusplattform.server;


import no.nav.statusplattform.AppConfig;
import no.nav.statusplattform.AppConfig.AzureConfig;
import no.nav.statusplattform.AppConfig.DbConfig;
import no.nav.statusplattform.api.StatusplattformApi;
import no.nav.statusplattform.api.SwaggerDocumentation;
import no.nav.statusplattform.infrastructure.RedirectHandler;
import no.nav.statusplattform.jobs.JobHandler;
import org.actioncontroller.config.ConfigObserver;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class StatusplattformServer {
    private static final Logger logger = LoggerFactory.getLogger(StatusplattformServer.class);

    private final Server server = new Server();
    private final ServerConnector connector = new ServerConnector(server);
    private final StatusplattformApi statusplattformApi = new StatusplattformApi("/rest");
    private final SwaggerDocumentation swaggerDocumentation = new SwaggerDocumentation("/doc");
    public final AppConfig appConfig = new AppConfig(new DbConfig(), new AzureConfig());
    private final JobHandler jobHandler = new JobHandler();


    public StatusplattformServer() {
        HttpConfiguration config = new HttpConfiguration();
        config.addCustomizer(new ForwardedRequestCustomizer());
        config.setSendServerVersion(false);
        config.setSendXPoweredBy(false);
        connector.addConnectionFactory(new HttpConnectionFactory(config));
        server.setHandler(new HandlerList(
              new RedirectHandler("/", "/doc"),
                statusplattformApi,
                swaggerDocumentation
                ));
        setupConfiguration();
    }

    private void setupConfiguration() {
        setFrontEndLocation(this.appConfig.frontendCorsValue);
        setDataSource(DataSourceTransformer.create(this.appConfig.dbConfig));

        // TODO: Figure out how to handle these (static?) classes' configuration
        //new AccessTokenHelper().setupAccessToken(this.appConfig.azConfig);
        //setTeamkatalogApiUrl(this.appConfig.teamkatalogApi);

        connector.setPort(this.appConfig.port);


        new ConfigObserver("portal")
                .onInetSocketAddress("http.port", this.appConfig.port, this::setHttpPort)
        ;
    }

    private void setFrontEndLocation(String frontEndLocation){
        statusplattformApi.setFrontEndLocation(frontEndLocation);
    }

    private void setDataSource(DataSource dataSource) {
        jobHandler.setDataSource(dataSource);
        statusplattformApi.setDataSource(dataSource);
    }


    public void setHttpPort(InetSocketAddress port) throws Exception {
        if (connector.getPort() == port.getPort()) {
            return;
        }
        connector.setPort(port.getPort());
        if (server.isStarted()) {
            connector.stop();
            connector.start();
            logger.warn("Started on {}", getURI());
        }
    }

    public void start() throws Exception {
        jobHandler.start();
        server.start();
        connector.start();
        logger.warn("Started on {}", getURI());
    }

    public URI getURI() throws URISyntaxException {
        return new URI("http://localhost:" + connector.getLocalPort());
    }

    public static void main(String[] args) throws Exception {
        new StatusplattformServer().start();
    }
}
