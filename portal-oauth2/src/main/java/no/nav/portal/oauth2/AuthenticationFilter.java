package no.nav.portal.oauth2;


import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import no.nav.portal.infrastructure.PortalRestPrincipal;
import no.nav.security.token.support.core.configuration.IssuerProperties;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.http.HttpRequest;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import no.nav.security.token.support.core.validation.DefaultJwtTokenValidator;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.jsonbuddy.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.validation.JwtTokenValidationHandler;

import javax.security.auth.Subject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


public class AuthenticationFilter implements Filter {
    private static String CLIENT_ID = System.getenv("AZURE_APP_CLIENT_ID");
    private static String CLIENT_SECRET = System.getenv("AZURE_APP_CLIENT_SECRET");
    private static String PUBLIC_JWKS_URI = System.getenv("AZURE_OPENID_CONFIG_JWKS_URI");
    private static String AZURE_OPENID_CONFIG_ISSUER = System.getenv("AZURE_OPENID_CONFIG_ISSUER");
    private static URL AZURE_WELL_KNOW_URL;
    private static String FRONTEND_LOCATION;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static URL PUBLIC_JWKS_URL;
    //private final Authentication authentication;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private  JwtTokenValidationHandler jwtTokenValidationHandler;



    static {
        try{
            /*
            AZURE_WELL_KNOW_URL = new URL(System.getenv("AZURE_APP_WELL_KNOWN_URL"));
            FRONTEND_LOCATION = System.getenv("FRONTEND_LOCATION");
            PUBLIC_JWKS_URL = new URL(PUBLIC_JWKS_URI);


             */
        }
        catch (Exception e){
            logger.info(e.toString());

        }
    }
    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {    }




    public AuthenticationFilter(Authentication authentication) {
        /*
        this.authentication = authentication;
        IssuerProperties issuerProperties = new  IssuerProperties(AZURE_WELL_KNOW_URL, List.of("api://"+CLIENT_ID,CLIENT_ID) );
        //IssuerProperties(AZURE_WELL_KNOW_URL, List<String> acceptedAudience, String cookieName, IssuerProperties.Validation validation, IssuerProperties.JwksCache jwksCache)
        //IssuerProperties issuerProperties2 = new IssuerProperties(PUBLIC_JWKS_URL);
        logger.info("IN Authenticationfilter: ");
        logger.info("AZURE_WELL_KNOW_URL: " +AZURE_WELL_KNOW_URL.toString() );
        //readPublicJwt();
        try {
            Map<String, IssuerProperties> issuerPropertiesMap = new HashMap<>();
            logger.info(issuerProperties.toString());
            issuerPropertiesMap.put("AzureAd", issuerProperties);
            logger.info("Trying to create: MultiIssuerConfiguration: " );
            logger.info("Issuer properties: "+ issuerProperties);
            MultiIssuerConfiguration multiIssuerConfiguration = new MultiIssuerConfiguration(issuerPropertiesMap);
            this.jwtTokenValidationHandler = new JwtTokenValidationHandler(multiIssuerConfiguration);
        }
        catch (Exception e){
            logger.info("Error in constructiong JwtTokenValidationHandler");
            logger.info(e.getMessage());
        }*/
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.info("IN AUTHENTICATION FILTER");
        chain.doFilter(request,response);
        /*
        MDC.clear();
        ((Request)request).setAuthentication(authentication);

        String pathInfo = ((Request) request).getPathInfo();
        logger.info("Path: "+ pathInfo);

        if (pathInfo.startsWith("/login")) {
            redirectToAuthorize((HttpServletRequest) request, (HttpServletResponse) response);
            return;
        }
        if ( pathInfo.startsWith("/callback")) {
            logger.info("Trying to create user for request in /callback");
            readAuthorizationFromHeader(request);
            doTokenValidation((HttpServletRequest)request);
            callBack((HttpServletRequest)request,(HttpServletResponse) response);
            return;
        }
        //Legg til principal her? for alle some ikke er login eller callback
        // se linje 120 i OpenIdConnectAuthentication
        logger.info("Trying to create user for request to " + pathInfo);
        JwtTokenClaims jwtTokenClaims = readAuthorizationFromHeader(request);
        if(jwtTokenClaims == null){
            chain.doFilter(request,response);
            return;
        }
        //Create userprinciple from header
        //Legg til refresh token funksjonalitet
        PortalRestPrincipal principal = createPortalPrinciplaFromAdClaims(jwtTokenClaims);

        Authentication authenticationForUser = new UserAuthentication("user", createUserIdentity(principal));
        ((Request) request).setAuthentication(authenticationForUser);
        chain.doFilter(request, response);*/
    }
/*
    protected Authentication redirectToAuthorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("redirectToAuthorize ---------------------------");

        response.sendRedirect("https://digitalstatus.ekstern.dev.nav.no" +"/oauth2/login?redirect="+ "/authenticate/callback");

        return Authentication.SEND_CONTINUE;
    }
    protected Authentication callBack(HttpServletRequest request, HttpServletResponse response) throws IOException {


        //DefaultJwtTokenValidator tokenValidator = new DefaultJwtTokenValidator(AZURE_OPENID_CONFIG_ISSUER,List.of(CLIENT_ID),new RemoteJWKSet(AZURE_WELL_KNOW_URL));

        response.sendRedirect(FRONTEND_LOCATION);
        return Authentication.SEND_CONTINUE;
    }

    private JwtTokenClaims readAuthorizationFromHeader(ServletRequest request) {
        String encodedAuthentication = ((HttpServletRequest) request).getHeader(AUTHORIZATION_HEADER);
        logger.info("In readAuthorizationFromHeader : ");
        logger.info("encodedAuthentication: " + encodedAuthentication);

        if (encodedAuthentication == null || encodedAuthentication.isEmpty()) {
            return null;
        }
        try {
            //TODO linja under fjerner "Bearer " fra starten av encodedAuth, denne bør kunne leses smudere
            encodedAuthentication = encodedAuthentication.substring(7,encodedAuthentication.length()-1);
            JwtToken jwtToken = new JwtToken(encodedAuthentication);
            return jwtToken.getJwtTokenClaims();

         }   catch (Exception e){
            logger.info("Couldt not read encoded authentication: "+ encodedAuthentication);
            logger.info(e.getMessage());
        }
        return null;
    }
    private JwtToken getJwtToken(ServletRequest request) {
        String encodedAuthentication = ((HttpServletRequest) request).getHeader(AUTHORIZATION_HEADER);
        logger.info("In readAuthorizationFromHeader : ");
        logger.info("encodedAuthentication: " + encodedAuthentication);

        if (encodedAuthentication == null || encodedAuthentication.isEmpty()) {
            return null;
        }
        try {
            //TODO linja under fjerner "Bearer " fra starten av encodedAuth, denne bør kunne leses smudere
            encodedAuthentication = encodedAuthentication.substring(7,encodedAuthentication.length()-1);
            return new JwtToken(encodedAuthentication);

        }   catch (Exception e){
            logger.info("Couldt not read encoded authentication: "+ encodedAuthentication);
            logger.info(e.getMessage());
        }
        return null;
    }

    private void doTokenValidation(HttpServletRequest request) {
        logger.info("In doTokenValidation");
        Map<String, JwtToken> issuerShortNameValidatedTokenMap = new HashMap<>();
        issuerShortNameValidatedTokenMap.put("AzureAd", getJwtToken(request));
        HttpRequest httpRequest = mapToNavSecuretyHttpRequest(request);
        TokenValidationContext context =  jwtTokenValidationHandler.getValidatedTokens(httpRequest);
        logger.info(context.toString());


    }

    private HttpRequest mapToNavSecuretyHttpRequest(HttpServletRequest httpServletRequest){
       return new HttpRequest() {
            @Override
            public String getHeader(String headerName) {
                return httpServletRequest.getHeader(headerName);
            }

            @Override
            public NameValue[] getCookies() {
                return Arrays.stream(httpServletRequest.getCookies())
                        .map(cookie -> new NameValue() {
                            @Override
                            public String getName() {
                                return cookie.getName();
                            }

                            @Override
                            public String getValue() {
                                return cookie.getValue();
                            }

                        }).collect(Collectors.toList()).toArray(NameValue[]::new);
            }
        };
    }

    private void doTokenValidation2(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.info("In doTokenValidation");
        Map<String, JwtToken> issuerShortNameValidatedTokenMap = new HashMap<>();
        issuerShortNameValidatedTokenMap.put("AzureAd", getJwtToken(request));
        TokenValidationContextHolder contextHolder = new TokenValidationContextHolder() {
            @Override
            public TokenValidationContext getTokenValidationContext() {
                return null;
            }

            @Override
            public void setTokenValidationContext(TokenValidationContext tokenValidationContext) {

            }
        };
        contextHolder.setTokenValidationContext(this.jwtTokenValidationHandler.getValidatedTokens(fromHttpServletRequest(request)));


        try {
            chain.doFilter(request, response);
        } finally {
            contextHolder.setTokenValidationContext((TokenValidationContext)null);
        }

    }


    public PortalRestPrincipal createPortalPrinciplaFromAdClaims(JwtTokenClaims jwtTokenClaims){
        System.out.println("createPrincipal ---------------------------");
        //payloadJson =
        return new PortalRestPrincipal(jwtTokenClaims.getStringClaim("name"), jwtTokenClaims.getStringClaim("NAVident"));
    }

    private DefaultUserIdentity createUserIdentity(Principal principal) {
        System.out.println("createUserIdentity ---------------------------");
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return new DefaultUserIdentity(subject, principal, new String[0]);
    }



    static HttpRequest fromHttpServletRequest(final HttpServletRequest request) {
        return new HttpRequest() {
            public String getHeader(String headerName) {
                return request.getHeader(headerName);
            }

            public NameValue[] getCookies() {
                return request.getCookies() == null ? null : (NameValue[])Arrays.stream(request.getCookies()).map((cookie) -> {
                    return new NameValue() {
                        public String getName() {
                            return cookie.getName();
                        }

                        public String getValue() {
                            return cookie.getValue();
                        }
                    };
                }).toArray((x$0) -> {
                    return new NameValue[x$0];
                });
            }
        };
    }


    private void readPublicJwt(){
        try {
            logger.info("Trying to read public jwt");

            HttpURLConnection con = (HttpURLConnection) PUBLIC_JWKS_URL.openConnection(); //Http URL Connection Created...
            BufferedReader br = null;
            if (100 <= con.getResponseCode() && con.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            con.disconnect();
            String publicJwt = sb.toString();
            logger.info("publicJWt:" + publicJwt);

            logger.info("Connection Response Message : "+con.getResponseMessage());
        }
        catch (Exception e) {
            logger.info( e.getMessage());
        }
    }*/
}
