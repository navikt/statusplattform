package no.nav.portal.infrastructure;

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
        if (((Request) request).getServletPath().startsWith("/login")) {
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
