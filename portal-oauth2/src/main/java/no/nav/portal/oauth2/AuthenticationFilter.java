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
    public AuthenticationFilter(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MDC.clear();
        ((Request)request).setAuthentication(authentication);
        String pathInfo = ((Request) request).getPathInfo();
        if (pathInfo.startsWith("/login") || pathInfo.startsWith("/callback")) {
            ((HttpServletRequest)request).authenticate((HttpServletResponse)response);
            return;
        }
        if (pathInfo.startsWith("/logout")) {
            ((OpenIdConnectAuthentication) authentication).logout(((HttpServletRequest)request), (HttpServletResponse)response);
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
