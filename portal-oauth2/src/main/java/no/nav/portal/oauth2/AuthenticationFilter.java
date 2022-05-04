package no.nav.portal.oauth2;


import no.nav.portal.infrastructure.OpenIdConnectAuthentication;
import no.nav.portal.infrastructure.PortalRestPrincipal;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.jsonbuddy.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


import javax.security.auth.Subject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Optional;


public class AuthenticationFilter implements Filter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final Authentication authentication;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    public AuthenticationFilter(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        logger.info("IN AUTHENTICATION FILTER");

        MDC.clear();
        ((Request)request).setAuthentication(authentication);

        String pathInfo = ((Request) request).getPathInfo();
        logger.info("Path: "+ pathInfo);
        getUserv2(request);

        if (pathInfo.startsWith("/login")) {
            ((HttpServletRequest)request).authenticate((HttpServletResponse)response);
            return;
        }
        if ( pathInfo.startsWith("/callback")) {
            logger.info("Trying to create user for request in /callback");
            getJsonBody(request);

            ((HttpServletRequest)request).authenticate((HttpServletResponse)response);
            return;
        }
        //Legg til principal her? for alle some ikke er login eller callback
        // se linje 120 i OpenIdConnectAuthentication
        logger.info("Trying to create user for request");
        JsonObject jsonObject = getJsonBody(request);
        if(jsonObject == null){
            chain.doFilter(request,response);
            return;
        }
        PortalRestPrincipal principal = createPrincipalv2(jsonObject);

        Authentication authenticationForUser = new UserAuthentication("user", createUserIdentity(principal));
        ((Request) request).setAuthentication(authenticationForUser);
        chain.doFilter(request, response);
    }

    public PortalRestPrincipal createPrincipalv2(JsonObject payloadJson){
        System.out.println("createPrincipal ---------------------------");
        //payloadJson =
        return new PortalRestPrincipal(payloadJson.requiredString("name"), payloadJson.stringValue("NAVident").orElse(null));
    }

    private DefaultUserIdentity createUserIdentity(Principal principal) {
        System.out.println("createUserIdentity ---------------------------");
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return new DefaultUserIdentity(subject, principal, new String[0]);
    }
    private void getUserv2(ServletRequest servletRequest) {
        logger.info("IN AUTHENTICATION FILTER, /authenticate");
        try {

            String encodedAuthentication = ((HttpServletRequest) servletRequest).getHeader(AUTHORIZATION_HEADER);

            if (encodedAuthentication == null || encodedAuthentication.isEmpty()) {
                return;
            }
            logger.info("Encoded: "+ encodedAuthentication);
            String[] splited = encodedAuthentication.split("[.]");

            String encodedHeader = splited[0];
            String encodedPayload = splited[1];
            //String encodedSignature = splited[2];

            String decodedHeader = new String(Base64.getDecoder().decode(encodedHeader));
            String decodedPayload = new String(Base64.getDecoder().decode(encodedPayload));

            JsonObject headerJson = JsonObject.parse(decodedHeader);
            JsonObject payloadJson = JsonObject.parse(decodedPayload);

            logger.info("Useridentity: " + headerJson + payloadJson);
        }
        catch (Exception e){
            logger.info("Could not read authorization token");
            logger.info(e.getMessage());
            Arrays.stream(e.getStackTrace()).forEach(stackTraceElement -> logger.info(stackTraceElement.toString()));
        }

    }

    private JsonObject getJsonBody(ServletRequest request) {
        String encodedAuthentication = ((HttpServletRequest) request).getHeader(AUTHORIZATION_HEADER);
        if (encodedAuthentication == null || encodedAuthentication.isEmpty()) {
            return null;
        }
        try {
            logger.info("In get jsonBody, encodedAuthentication: "+ encodedAuthentication);

            String[] splited = encodedAuthentication.split("[.]");
            String encodedHeader = splited[0];
            String encodedPayload = splited[1];
            String encodedSignature = splited[2];
            //byte[] decodedBytes = Base64.getDecoder().decode(encodedAuthentication);
            String decodedHeader = new String(Base64.getDecoder().decode(encodedHeader));
            String decodedPayload = new String(Base64.getDecoder().decode(encodedPayload));
            logger.info("decodedHeader: "+ encodedAuthentication);
            logger.info("decodedPayload: "+ encodedAuthentication);
            JsonObject headerJson = JsonObject.parse(decodedHeader);
            JsonObject payloadJson = JsonObject.parse(decodedPayload);
            return payloadJson;
         }   catch (Exception e){
            logger.info("Couldt not read encoded authentication: "+ encodedAuthentication);
            logger.info(e.getMessage());
        }
        return null;
    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }
}
