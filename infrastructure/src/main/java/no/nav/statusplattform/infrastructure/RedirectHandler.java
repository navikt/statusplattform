package no.nav.statusplattform.infrastructure;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RedirectHandler extends AbstractHandler {
    private final String contextPath;
    private final String target;

    public RedirectHandler(String contextPath, String target) {
        this.contextPath = contextPath;
        this.target = target;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (target.equals(contextPath)) {
            response.sendRedirect(this.target);
        }
    }
}
