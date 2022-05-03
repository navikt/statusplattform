package no.nav.portal.oauth2;


import no.nav.portal.infrastructure.OpenIdConnectAuthentication;
import no.nav.portal.infrastructure.PortalRestPrincipal;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.jsonbuddy.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
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

       /**
         if (pathInfo.startsWith("/login") ) {
            ((HttpServletResponse) response).sendRedirect(backendLocation +"/oauth2/login?redirect="+ frontendLocation);
            return;
       }
        if ( pathInfo.startsWith("/callback")) {
            ((HttpServletResponse) response).sendRedirect(backendLocation + "/oauth2/callback");
            return;
        }
        if (pathInfo.startsWith("/logout")) {
            ((HttpServletResponse) response).sendRedirect(backendLocation+ "/oauth2/logout");
            return;
        }**/
        if (pathInfo.startsWith("/login") || pathInfo.startsWith("/callback")) {
            ((HttpServletRequest)request).authenticate((HttpServletResponse)response);
            return;
        }
        if( pathInfo.startsWith("/authenticate")){
            getUserv2(request);
        }
        chain.doFilter(request, response);
    }

    private void getUserv2(ServletRequest servletRequest) {
        logger.info("IN AUTHENTICATION FILTER, /authenticate");

        String encodedAuthentication = ((HttpServletRequest) servletRequest).getHeader(AUTHORIZATION_HEADER);

        if(encodedAuthentication == null || encodedAuthentication.isEmpty() ){
            return;
        }
        String[] splited = encodedAuthentication.split("[.]");

        String encodedHeader = splited[0];
        String encodedPayload = splited[1];
        String encodedSignature = splited[2];

        String decodedHeader  = new String(Base64.getDecoder().decode(encodedHeader));
        String decodedPayload  = new String(Base64.getDecoder().decode(encodedPayload));

        JsonObject headerJson =  JsonObject.parse(decodedHeader);
        JsonObject payloadJson = JsonObject.parse(decodedPayload);

        logger.info("Useridentity: "+ headerJson + payloadJson);

    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }
}
