package no.nav.portal.infrastructure;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class CORSFilter
 */

public class CORSFilter implements Filter {


    private static String frontendLocation;
    private Logger logger =  LoggerFactory.getLogger(CORSFilter.class);


    public CORSFilter() {
    }

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // Authorize (allow) all domains to consume the content
        //TODO hvordan skal dette håndteres? Dersom frontend og backend hostes på samme server trenger ikke

       ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", frontendLocation);
       ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST, DELETE");
       ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Headers", "*");


        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        // For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS handshake
        if (request.getMethod().equals("OPTIONS")) {
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            ((HttpServletResponse) servletResponse).addHeader("Access-Control-max-age", "600");
            return;
        }

        // pass the request along the filter chain
        chain.doFilter(request, servletResponse);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
        // TODO Auto-generated method stub
    }

    public void setFrontendLocation(String frontendLocation) {
        this.frontendLocation = frontendLocation;
    }
}