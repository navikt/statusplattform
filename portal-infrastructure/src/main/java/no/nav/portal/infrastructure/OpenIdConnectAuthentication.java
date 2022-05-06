package no.nav.portal.infrastructure;


import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.fluentjdbc.util.ExceptionUtil;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.security.token.support.core.validation.DefaultJwtTokenValidator;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import no.nav.security.token.support.core.validation.JwtTokenValidationHandler;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import java.util.*;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.URLEncoder.encode;

public class OpenIdConnectAuthentication implements Authentication.Deferred {
    private static final Logger logger = LoggerFactory.getLogger(OpenIdConnectAuthentication.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    /*
    public static final String ID_TOKEN_COOKIE = "tmp_token";
    public static final String AUTHORIZATION_STATE_COOKIE = "authorization_state";

    private CachedHashMap<String, Principal> cache = new CachedHashMap<>(Duration.ofMinutes(1));


    private static String CLIENT_ID = System.getenv("AZURE_APP_CLIENT_ID");
    private static String CLIENT_SECRET = System.getenv("AZURE_APP_CLIENT_SECRET");
    private static String PUBLIC_JWKS = System.getenv("AZURE_OPENID_CONFIG_JWKS_URI");
    private static String AZURE_OPENID_CONFIG_ISSUER = System.getenv("AZURE_OPENID_CONFIG_ISSUER");
    private static URL AZURE_WELL_KNOW_URL;
    private static String FRONTEND_LOCATION;


    static {
        try{
            AZURE_WELL_KNOW_URL = new URL(System.getenv("AZURE_APP_WELL_KNOWN_URL"));
            FRONTEND_LOCATION = System.getenv("FRONTEND_LOCATION");

        }
        catch (MalformedURLException e){
            logger.info(e.toString());

        }
    }
    */

    protected Authentication oauth2callback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("oauth2callback ---------------------------");
        /*
        String authorization = request.getHeader("Authorization");
        Enumeration<String> headers = request.getHeaderNames();
        logger.info("autorization: " + authorization);
        logger.info("autorization decoded: " + decodeBase64Url(authorization.getBytes(StandardCharsets.UTF_8)) );
        while(headers.hasMoreElements()){
            logger.info("Header element: "+   headers.nextElement());
        }



        DefaultJwtTokenValidator tokenValidator = new DefaultJwtTokenValidator(AZURE_OPENID_CONFIG_ISSUER,List.of(CLIENT_ID),new RemoteJWKSet(AZURE_WELL_KNOW_URL));

        response.sendRedirect(FRONTEND_LOCATION);

*/

        return Authentication.SEND_CONTINUE;
    }



    @Override
    public Authentication authenticate(ServletRequest servletRequest) {
        logger.info("authenticate(ServletRequest servletRequest) ---------------------------");
        //Sette verdier fra header isteden for cookie
        return getUserv2(servletRequest)
                .orElse(this);
    }

    private Optional<Authentication> getUserv2(ServletRequest servletRequest) {
        logger.info("getUser ---------------------------");

        String encodedAuthentication = ((HttpServletRequest) servletRequest).getHeader(AUTHORIZATION_HEADER);
        if(encodedAuthentication == null || encodedAuthentication.isEmpty()){
            return Optional.empty();
        }
        String[] splited = encodedAuthentication.split("[.]");

        String encodedHeader = splited[0];
        String encodedPayload = splited[1];
        String encodedSignature = splited[2];

        String decodedHeader  = new String(Base64.getDecoder().decode(encodedHeader));
        String decodedPayload  = new String(Base64.getDecoder().decode(encodedPayload));

        JsonObject headerJson =  JsonObject.parse(decodedHeader);
        JsonObject payloadJson = JsonObject.parse(decodedPayload);
        PortalRestPrincipal principal = createPrincipalv2(payloadJson);

        logger.info("Useridentity: "+ principal.getName() + principal.getNavIdent());
        return Optional.of(new UserAuthentication("user", createUserIdentity(principal)));
    }

    public PortalRestPrincipal createPrincipalv2(JsonObject payloadJson){
        System.out.println("createPrincipal ---------------------------");
        logger.info(payloadJson.toJson());
        return new PortalRestPrincipal(payloadJson.requiredString("name"), payloadJson.stringValue("NAVident").orElse(null));
    }

    @Override
    public Authentication authenticate(ServletRequest servletRequest, ServletResponse servletResponse) {

        System.out.println("authenticate ---------------------------");
        try {
            return doAuthenticate((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Authentication doAuthenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Request uri = " + request.getRequestURI());
        if (request.getRequestURI().startsWith(request.getContextPath() + "/callback")) {
            return oauth2callback(request, response);
        } else if (request.getRequestURI().startsWith(request.getContextPath() + "/login")) {
            return redirectToAuthorize(request, response);
        }
        else if (request.getRequestURI().startsWith(request.getContextPath() + "/logout")) {
            return logout(request);
        }
        return null;
    }

    @Override
    public Authentication login(String s, Object o, ServletRequest servletRequest) {
        return null;
    }







    private DefaultUserIdentity createUserIdentity(Principal principal) {
        System.out.println("createUserIdentity ---------------------------");
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return new DefaultUserIdentity(subject, principal, new String[0]);
    }

    protected Authentication redirectToAuthorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("redirectToAuthorize ---------------------------");




        response.sendRedirect("https://digitalstatus.ekstern.dev.nav.no" +"/oauth2/login?redirect="+ "/authenticate/callback");

        return Authentication.SEND_CONTINUE;
    }

    private String decodeBase64Url(byte[] encoded){
        return Base64.getUrlEncoder().encodeToString(encoded);
    }











    @Override
    public Authentication logout(ServletRequest request) {
        return null;
    }
}
