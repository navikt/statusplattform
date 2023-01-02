package no.nav.portal.infrastructure;

import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.*;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
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
        logger.info("In token Validator");
        String accessToken =  ((HttpServletRequest) request).getHeader(AUTHORIZATION_HEADER);
        logger.info("access token: "+ accessToken);
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

    public String readAccessTokenFromHeader(ServletRequest request){
        String BEARER ="Bearer ";

       // String accessToken =
        //        "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjJaUXBKM1VwYmpBWVhZR2FYRUpsOGxWMFRPSSJ9.eyJhdWQiOiI2YTdlNjhiMC04ODZiLTRiZDItOGU5YS0yYTU4ZTU5YzE2MzIiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vOTY2YWM1NzItZjViNy00YmJlLWFhODgtYzc2NDE5YzBmODUxL3YyLjAiLCJpYXQiOjE2NjkyODcwNzksIm5iZiI6MTY2OTI4NzA3OSwiZXhwIjoxNjY5MjkwOTc5LCJhaW8iOiJFMlpnWU5CZXIyTi83T0E5ejhCNUQxb2x6cjZiQ3dBPSIsImF6cCI6ImQ4Zjg2ZWY5LTE3MWQtNGRiZC04MjA5LWRhODg4YWU0NTcyNCIsImF6cGFjciI6IjEiLCJvaWQiOiIwMzU4YTMyNC0zYzM3LTQyMTgtOTgzNy1kYmQzMDc0MzIyOWIiLCJyaCI6IjAuQVVjQWNzVnFscmYxdmt1cWlNZGtHY0Q0VWJCb2ZtcHJpTkpManBvcVdPV2NGakpIQUFBLiIsInJvbGVzIjpbImFjY2Vzc19hc19hcHBsaWNhdGlvbiJdLCJzdWIiOiIwMzU4YTMyNC0zYzM3LTQyMTgtOTgzNy1kYmQzMDc0MzIyOWIiLCJ0aWQiOiI5NjZhYzU3Mi1mNWI3LTRiYmUtYWE4OC1jNzY0MTljMGY4NTEiLCJ1dGkiOiJtWGlGXzQxQnNFZTI5bjE0SS1YTEFBIiwidmVyIjoiMi4wIn0.o3VteE2f0vGpEsUDZRjYEADFRJ6ugXBo9BQ934Bgc4ct4msecYXNK9HEFm7CAZeRdP5rN0HB6GE3PRV3Msd6CetayvMJFx0_IeDZOKUcPK9klzTAzXp3Yq482ojCtie52BZmK7cb0uVgg2vzE29BMRVmbZrr3eahGZ-OHh2kU0jV6v5LPgkbMYTXic7ZxjaYr4vr40hCiUtpt9Ym3VUpfULPwbaZJfXxVoz3PaOb5h1XAAsd8VJtzyL1nWanLq9XlaOlBQrqXLkmulpZoOmiexY9pKhyUGKkhs-GDEwM3LqXkW6BQYUD_hMiEgfI3enmdRUXB40DRL-AM3ofhKjMNg";
        String bearerToken = ((HttpServletRequest) request).getHeader(AUTHORIZATION_HEADER);

        if (bearerToken == null || bearerToken.isEmpty() ||!bearerToken.startsWith("Bearer ")) {
            return null;
        }
        //Removes "Bearer " from token
        return  bearerToken.substring(BEARER.length());

    }
}





