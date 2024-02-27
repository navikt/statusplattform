package no.nav.statusplattform.api.Helpers;


import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import no.nav.statusplattform.AppConfig.AzureConfig;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// TODO: Figure out if this class is in use elsewhere than just tests??
public class AccessTokenHelper {
    // Construct the client credentials grant
    private String TENANT;
    private ClientAuthentication clientAuth;
    private Scope scope;

    // TODO: Figure out how to integrate this w/config set-up when something here needs to be `new`ed.
    public void setupAccessToken(@NotNull AzureConfig config) {
        // Construct the client credentials grant
        this.clientAuth = new ClientSecretBasic(new ClientID(config.clientId), new Secret(config.clientSecret));
        this.scope = new Scope("api://" + config.oath2Scope + "-fss.navdig.statusholder/.default");
        this.TENANT = config.tenant;
    }

    public AccessToken getAccessTokenForStatusholder() throws URISyntaxException, IOException, ParseException {

        AuthorizationGrant clientGrant = new ClientCredentialsGrant();
        // The token endpoint
        URI tokenEndpoint =  new URI("https://login.microsoftonline.com/"+TENANT+"/oauth2/v2.0/token");

        // Make the token request
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, clientGrant, scope);
        TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

        if (!response.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = response.toErrorResponse();
        }

        // Get the access token
        return response.toSuccessResponse().getTokens().getAccessToken();
    }
}
