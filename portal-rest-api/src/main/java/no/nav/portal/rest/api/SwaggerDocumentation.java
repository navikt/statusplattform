package no.nav.portal.rest.api;

import no.nav.portal.infrastructure.ClasspathWebAppContext;
import no.nav.portal.infrastructure.WebJarServlet;
import org.eclipse.jetty.servlet.ServletHolder;

public class SwaggerDocumentation extends ClasspathWebAppContext {
    public SwaggerDocumentation(String context) {
        super(context, "/webapp-web");
        addServlet(new ServletHolder(new WebJarServlet("swagger-ui")), "/swagger/*");
    }
}
