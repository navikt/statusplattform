package no.nav.portal.infrastructure;

import org.jsonbuddy.JsonObject;
import org.jsonbuddy.pojo.PojoMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenIdConfiguration {

    public static OpenIdConfiguration read(URL openIdConfigurationUrl) throws IOException {
        return PojoMapper.mapType(JsonObject.read(openIdConfigurationUrl), OpenIdConfiguration.class);
    }

    private URL authorizationEndpoint;
    private URL tokenEndpoint;
    private URL userinfoEndpoint;
    private URL endSessionEndpoint;

    public HttpURLConnection openUserinfoConnection() throws IOException {
        return (HttpURLConnection) getUserinfoEndpoint().openConnection();
    }

    public HttpURLConnection openTokenConnection() throws IOException {
        return (HttpURLConnection) getTokenEndpoint().openConnection();
    }

    public URL getUserinfoEndpoint() {
        return userinfoEndpoint;
    }

    public void setUserinfoEndpoint(URL userinfoEndpoint) {
        this.userinfoEndpoint = userinfoEndpoint;
    }

    public URL getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(URL authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public URL getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(URL tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public URL getEndSessionEndpoint() {
        return endSessionEndpoint;
    }
    public HttpURLConnection openLogoutConnection() throws IOException{
        return (HttpURLConnection) getEndSessionEndpoint().openConnection();
    }

    public void setEndSessionEndpoint(URL endSessionEndpoint) {
        System.out.println("EndSession endpoint: " + endSessionEndpoint);
        this.endSessionEndpoint = endSessionEndpoint;

    }
}
