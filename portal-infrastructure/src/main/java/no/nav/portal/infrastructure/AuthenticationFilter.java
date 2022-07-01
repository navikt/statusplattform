package no.nav.portal.infrastructure;

import no.nav.security.token.support.core.configuration.IssuerProperties;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.http.HttpRequest;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import no.nav.security.token.support.core.validation.JwtTokenValidationHandler;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.security.auth.Subject;
import javax.security.sasl.AuthenticationException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

public class AuthenticationFilter implements Filter {


    public static final String AUTHORIZATION_HEADER = "authorization";
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private JwtTokenValidationHandler jwtTokenValidationHandler;
    private static String PUBLIC_JWKS_URI = System.getenv("AZURE_OPENID_CONFIG_JWKS_URI");
    private static String CLIENT_ID = System.getenv("AZURE_APP_CLIENT_ID");
    private static String CLIENT_SECRET = System.getenv("AZURE_APP_CLIENT_SECRET");
    private static String SWAGGER_API_KEY_HEADERNAME = "Apikey";
    private static String SWAGGER_API_KEY= System.getenv("swagger-api-key");
    public static URL PUBLIC_JWKS_URL;
    private static URL AZURE_WELL_KNOW_URL;

    static{
        try{
            //AZURE_WELL_KNOW_URL = new URL(System.getenv("AZURE_APP_WELL_KNOWN_URL"));
            //PUBLIC_JWKS_URL = new URL(PUBLIC_JWKS_URI);
        }
        catch (Exception e){
            logger.error("Could not read Azure urls in auth filter: "+ e);
         }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        //

        /*
        doTokenValidation((HttpServletRequest) request);*/
        if(validateSwaggerAuthentication(request)){
            chain.doFilter(request, response);
            MDC.clear();
            return;
        }
        JwtTokenClaims jwtTokenClaims = readAuthorizationFromHeader(request);
        if(List.of("POST","PUT","DELETE").contains(((HttpServletRequest) request).getMethod())
                && jwtTokenClaims == null){
            throw new AuthenticationException("Cant access closed endpoints, PUT, POST, DELETE, without authenticated user");
        }
        if(jwtTokenClaims != null){
            PortalRestPrincipal principal = createPortalPrinciplaFromAdClaims(jwtTokenClaims);
            Authentication authenticationForUser = new UserAuthentication("user", createUserIdentity(principal));
            ((Request) request).setAuthentication(authenticationForUser);
        }
        chain.doFilter(request, response);
        MDC.clear();
    }

    private boolean validateSwaggerAuthentication(ServletRequest request) {
        String apikey = ((HttpServletRequest) request).getHeader(SWAGGER_API_KEY_HEADERNAME);
        return SWAGGER_API_KEY.equals(apikey);


    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }
    public AuthenticationFilter(){/*
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
        }
            */

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



    private JwtTokenClaims readAuthorizationFromHeader(ServletRequest request) {
        String encodedAuthorization = ((HttpServletRequest) request).getHeader(AUTHORIZATION_HEADER);
        logger.info("In readAuthorizationFromHeader : ");
        //logger.info("encodedAuthorization: " + encodedAuthorization);
        Enumeration<String> headersNames =  ((HttpServletRequest) request).getHeaderNames();
        //logger.info("All headers: ");
        while (headersNames.hasMoreElements()){
            String headername = headersNames.nextElement();
            //logger.info(headername+ " " + ((HttpServletRequest) request).getHeader(headername));
        }
        if (encodedAuthorization == null || encodedAuthorization.isEmpty()) {
            return null;
        }
        try {
            //TODO linja under fjerner "Bearer " fra starten av encodedAuth, denne bør kunne leses smudere. Bruk JwtTokenValidationHandler.
            encodedAuthorization = encodedAuthorization.substring(7,encodedAuthorization.length()-1);
            JwtToken jwtToken = new JwtToken(encodedAuthorization);
//            logger.info("jwtToken: "+ jwtToken.getJwtTokenClaims());
            return jwtToken.getJwtTokenClaims();

        }   catch (Exception e){
            logger.info("Couldt not read encoded authentication: "+ encodedAuthorization);
            logger.info(e.getMessage());
        }
        return null;
    }
    /*
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

    private TokenValidationContext doTokenValidation(HttpServletRequest request) {
        logger.info("In doTokenValidation");
        Map<String, JwtToken> issuerShortNameValidatedTokenMap = new HashMap<>();
        JwtToken jwtToken = getJwtToken(request);
        if(jwtToken == null){
            return null;
        }

        issuerShortNameValidatedTokenMap.put("AzureAd", getJwtToken(request));
        HttpRequest navSecuretyHttpRequest = mapToNavSecuretyHttpRequest(request);
        TokenValidationContext context =  jwtTokenValidationHandler.getValidatedTokens(navSecuretyHttpRequest);
        logger.info(context.toString());
        return context;
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
*/

}
