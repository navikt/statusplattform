package no.nav.portal.oauth2;


import no.nav.portal.infrastructure.PortalRestPrincipal;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
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
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;


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

        if (pathInfo.startsWith("/login")) {
            ((HttpServletRequest)request).authenticate((HttpServletResponse)response);
            return;
        }
        if ( pathInfo.startsWith("/callback")) {
            logger.info("Trying to create user for request in /callback");
            readAuthorizationFromHeader(request);

            ((HttpServletRequest)request).authenticate((HttpServletResponse)response);
            return;
        }
        //Legg til principal her? for alle some ikke er login eller callback
        // se linje 120 i OpenIdConnectAuthentication
        logger.info("Trying to create user for request");
        JwtTokenClaims jwtTokenClaims = readAuthorizationFromHeader(request);
        if(jwtTokenClaims == null){
            chain.doFilter(request,response);
            return;
        }
        PortalRestPrincipal principal = createPortalPrinciplaFromAdClaims(jwtTokenClaims);

        Authentication authenticationForUser = new UserAuthentication("user", createUserIdentity(principal));
        ((Request) request).setAuthentication(authenticationForUser);
        chain.doFilter(request, response);
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


    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }
}
