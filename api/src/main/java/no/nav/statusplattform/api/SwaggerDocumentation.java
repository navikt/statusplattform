package no.nav.statusplattform.api;

import no.nav.statusplattform.infrastructure.ClasspathWebAppContext;
import no.nav.statusplattform.infrastructure.WebJarServlet;
import org.eclipse.jetty.servlet.ServletHolder;

public class SwaggerDocumentation extends ClasspathWebAppContext {
    public SwaggerDocumentation(String context) {
        super(context, "/webapp-web");
        addServlet(new ServletHolder(new WebJarServlet("swagger-ui")), "/swagger/*");
    }
}
