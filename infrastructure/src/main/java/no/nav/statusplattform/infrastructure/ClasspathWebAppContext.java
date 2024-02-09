package no.nav.statusplattform.infrastructure;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.net.URI;

public class ClasspathWebAppContext extends WebAppContext {
    public ClasspathWebAppContext(String contextPath, String baseResourcePath) {
        setContextPath(contextPath);
        Resource base = Resource.newClassPathResource(baseResourcePath);
        if (base == null) {
            throw new IllegalArgumentException("Missing resource directory " + baseResourcePath);
        }
        URI uri = base.getURI();
        if (uri.getScheme().equalsIgnoreCase("file")) {
            File resourceSrc = new File(uri.getPath().replace("/target/classes/", "/src/main/resources/"));
            if (resourceSrc.exists()) {
                setBaseResource(Resource.newResource(resourceSrc));
                this.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
            } else {
                setBaseResource(base);
            }
        } else {
            setBaseResource(base);
        }

    }
}
