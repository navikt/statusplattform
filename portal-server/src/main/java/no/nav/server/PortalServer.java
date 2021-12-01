package no.nav.server;


import nav.portal.polling.PortalPoller;
import no.nav.portal.infrastructure.RedirectHandler;
import no.nav.portal.oauth2.LogInnApi;
import no.nav.portal.rest.api.PortalRestApi;
import no.nav.portal.rest.api.SwaggerDocumentation;
import org.actioncontroller.config.ConfigObserver;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class PortalServer {
    private static final Logger logger = LoggerFactory.getLogger(PortalServer.class);

    private final Server server = new Server();
    private final ServerConnector connector = new ServerConnector(server);
    private final PortalRestApi portalRestApi = new PortalRestApi("/rest");
    private final LogInnApi logInnApi = new LogInnApi("/oauth2");
    private final PortalPoller portalPoller = new PortalPoller();
    private final SwaggerDocumentation swaggerDocumentation = new SwaggerDocumentation("/doc");

    public PortalServer() {
        HttpConfiguration config = new HttpConfiguration();
        config.addCustomizer(new ForwardedRequestCustomizer());
        config.setSendServerVersion(false);
        config.setSendXPoweredBy(false);
        connector.addConnectionFactory(new HttpConnectionFactory(config));
        server.setHandler(new HandlerList(
              new RedirectHandler("/", "/doc"),
                portalRestApi,
                logInnApi,
                swaggerDocumentation
                ));
        setupConfiguration();
    }

    private void setupConfiguration() {
        int port = Optional.ofNullable(System.getenv("HTTP_PLATFORM_PORT")).map(Integer::parseInt)
                .orElse(3005);
        connector.setPort(port);
        setDataSource(DataSourceTransformer.create());

        new ConfigObserver("portal")
                //.onPrefixedValue("dataSource", DataSourceTransformer::create, this::setDataSource)
                .onInetSocketAddress("http.port", port, this::setHttpPort)
                .onStringValue("isLocal", "false", this::setIsLocal)
                /*Når man legger inn autentisering med OIDC for Profil / Admin så er det ca slik. Se denne commit for hvordan det funket for rest før
                .onStringValue("openid.discovery_url", null, brukergrupperRestApi::setOpenIdConfiguration)
                .onStringValue("openid.client_id", null, brukergrupperRestApi::setClientId)
                .onStringValue("openid.client_secret", null, brukergrupperRestApi::setClientSecret)*/

        ;
    }

    private void setIsLocal(String isLocal) {
        boolean isLocalBool= Boolean.parseBoolean(isLocal);
        logInnApi.setIsLocal(isLocalBool);
        portalRestApi.setIsLocal(isLocalBool);
    }

    private void setDataSource(DataSource dataSource) {
        portalPoller.setDataSource(dataSource);
        portalRestApi.setDataSource(dataSource);
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
        server.start();
        connector.start();
        //portalPoller.start();
        logger.warn("Started on {}", getURI());
    }

    public URI getURI() throws URISyntaxException {
        return new URI("http://localhost:" + connector.getLocalPort());
    }

    public static void main(String[] args) throws Exception {
        //HttpsURLConnection.setDefaultSSLSocketFactory(CertificateTrustStore.loadFromDirectory(new File(".")).createSslSocketFactory());
        new PortalServer().start();
    }
}
