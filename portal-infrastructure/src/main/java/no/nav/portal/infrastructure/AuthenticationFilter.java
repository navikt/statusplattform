package no.nav.portal.infrastructure;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jwt.JWTClaimsSet;
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

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.*;


public class AuthenticationFilter implements Filter {


    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static String SWAGGER_API_KEY_HEADERNAME = "Apikey";
    private static String SWAGGER_API_KEY= System.getenv("swagger-api-key");
    private final Oauth2TokenValidator oauth2TokenValidator = new Oauth2TokenValidator();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(validateSwaggerAuthentication(request)){
            chain.doFilter(request, response);
            MDC.clear();
            return;
        }
        JWTClaimsSet jwtTokenClaims = oauth2TokenValidator.validateTokenAndGetClaims(request);
        if(List.of("POST","PUT","DELETE").contains(((HttpServletRequest) request).getMethod())
                && jwtTokenClaims == null){
            throw new AuthenticationException("Cant access closed endpoints, PUT, POST, DELETE, without authenticated user");
        }
        if(jwtTokenClaims != null){
            try {
                PortalRestPrincipal principal = createPortalPrinciplaFromAdClaims(jwtTokenClaims);
                Authentication authenticationForUser = new UserAuthentication("user", createUserIdentity(principal));
                ((Request) request).setAuthentication(authenticationForUser);
            }
            catch (ParseException e) {
                throw new AuthenticationException("Can't parse obligatory jwt claims");
            }

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
    public AuthenticationFilter(){

    }
    public PortalRestPrincipal createPortalPrinciplaFromAdClaims(JWTClaimsSet jwtTokenClaims) throws ParseException, AuthenticationException {
        String APPLICATION_ROLE = "access_as_application";
        Map<String,Object> claims = jwtTokenClaims.getClaims();
        if(claims.containsKey("name") && claims.containsKey("NAVident")){
            return new PortalRestPrincipal(jwtTokenClaims.getStringClaim("name"), jwtTokenClaims.getStringClaim("NAVident"));
        }
        if(claims.containsKey("roles")){
            JSONArray roles = (JSONArray)claims.get("roles");
            boolean isApplication = roles.size() == 1 && roles.get(0).equals(APPLICATION_ROLE);
            return new PortalRestPrincipal("application_access", "application_access");

        }
        else throw new AuthenticationException("Jwt claims does not cointain user, or application access");
    }

    private DefaultUserIdentity createUserIdentity(Principal principal) {
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return new DefaultUserIdentity(subject, principal, new String[0]);
    }





}
