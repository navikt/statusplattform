package no.nav.statusplattform.infrastructure;

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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


public class AuthenticationFilter implements Filter {


    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static String SWAGGER_API_KEY_HEADERNAME = "Apikey";
    // TODO: Replace w/AppConfig
    private static String SWAGGER_API_KEY= System.getenv("SWAGGER_API_KEY");
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
                StatusplattformRestPrincipal principal = createPortalPrincipalFromAdClaims(jwtTokenClaims);
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
    public StatusplattformRestPrincipal createPortalPrincipalFromAdClaims(JWTClaimsSet jwtTokenClaims) throws ParseException, AuthenticationException {
        String DEFAULT_ACCESS = "defaultaccess";
        Map<String,Object> claims = jwtTokenClaims.getClaims();
        if(claims.containsKey("scp") && claims.get("scp").equals(DEFAULT_ACCESS)){
            return new StatusplattformRestPrincipal(jwtTokenClaims.getStringClaim("name"), null);
        }
        if(claims.containsKey("roles")){
            return new StatusplattformRestPrincipal("application_access", "application_access");

        }
        else throw new AuthenticationException("Jwt claims does not cointain user, or application access");
    }

    private DefaultUserIdentity createUserIdentity(Principal principal) {
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return new DefaultUserIdentity(subject, principal, new String[0]);
    }
}
