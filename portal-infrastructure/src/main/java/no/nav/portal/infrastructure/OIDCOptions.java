package no.nav.portal.infrastructure;

import java.net.URL;

public class OIDCOptions {
    public final URL openIdConfiguration;
    public final String clientId;
    public final String clientSecret;

    public OIDCOptions(URL openIdConfiguration, String clientId, String clientSecret) {
        this.openIdConfiguration = openIdConfiguration;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}