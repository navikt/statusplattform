package no.nav.portal.infrastructure;

import com.google.gson.Gson;
import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;

public class Oauth2TokenValidator {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2TokenValidator.class);
    private String remoteJWKUrl = System.getenv("AZURE_OPENID_CONFIG_JWKS_URI");
    private String configIssuer = System.getenv("AZURE_OPENID_CONFIG_ISSUER");
    public static final String AUTHORIZATION_HEADER = "authorization";


    public JWTClaimsSet  validateTokenAndGetClaims(ServletRequest request) {
        String accessToken =  readAccessTokenFromHeader(request);
        try {
            return doValidateAndGetClaims(accessToken);
        } catch (Exception e) {
            return null;
        }
    }

    public JWTClaimsSet doValidateAndGetClaims(String accessToken) throws BadJOSEException, ParseException, JOSEException, MalformedURLException {
        // The access token to validate, typically submitted with a HTTP header like
        // Authorization: Bearer eyJraWQiOiJDWHVwIiwidHlwIjoiYXQrand0IiwiYWxnIjoi...


        // Create a JWT processor for the access tokens
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor =
                new DefaultJWTProcessor<>();

        // Set the required "typ" header "at+jwt" for access tokens issued by the
        // Connect2id server, may not be set by other servers
        jwtProcessor.setJWSTypeVerifier(
                new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("JWT")));

        // The public RSA keys to validate the signatures will be sourced from the
        // OAuth 2.0 server's JWK set, published at a well-known URL. The RemoteJWKSet
        // object caches the retrieved keys to speed up subsequent look-ups and can
        // also handle key-rollover
        JWKSource<SecurityContext> keySource =
                new RemoteJWKSet<>(new URL(remoteJWKUrl));

        // The expected JWS algorithm of the access tokens (agreed out-of-band)
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

        // Configure the JWT processor with a key selector to feed matching public
        // RSA keys sourced from the JWK set URL
        JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);

        jwtProcessor.setJWSKeySelector(keySelector);

        // Set the required JWT claims for access tokens issued by the Connect2id
        // server, may differ with other servers
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier(
                new JWTClaimsSet.Builder().issuer(configIssuer).build(),
                new HashSet<>(Arrays.asList("sub", "iat", "exp"))));

        // Process the token
        SecurityContext ctx = null; // optional context parameter, not required here
        return jwtProcessor.process(accessToken, ctx);
    }

    private String readAccessTokenFromHeader(ServletRequest request){

        String oboTokenString = ((HttpServletRequest) request).getHeader(AUTHORIZATION_HEADER);
        if (oboTokenString == null || oboTokenString.isEmpty()) {
            return null;
        }
        Gson g = new Gson();
        OBOtoken oboToken = g.fromJson(oboTokenString,OBOtoken.class);

        return oboToken.getAccess_token();

    }
}





