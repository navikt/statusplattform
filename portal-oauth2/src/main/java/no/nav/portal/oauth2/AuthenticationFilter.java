package no.nav.portal.oauth2;

import no.nav.portal.infrastructure.OpenIdConnectAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    private final Authentication authentication;
    private final String frontendLocation;
    private final String backendLocation;

    public AuthenticationFilter(Authentication authentication, String frontendLocation) {
        this.frontendLocation = frontendLocation;
        this.authentication = authentication;
        this.backendLocation = System.getenv("BACKEND_LOCATION");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MDC.clear();
        ((Request)request).setAuthentication(authentication);
        String pathInfo = ((Request) request).getPathInfo();
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
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }
}
