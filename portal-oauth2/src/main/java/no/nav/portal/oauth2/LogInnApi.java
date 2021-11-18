package no.nav.portal.oauth2;

import no.nav.portal.infrastructure.*;
import org.actioncontroller.servlet.ApiServlet;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;

public class LogInnApi extends ClasspathWebAppContext {

        private final Authentication authentication = new OpenIdConnectAuthentication();

        private final CORSFilter corsFilter;

        public LogInnApi(String context) {
            super(context, "/webapp-web");
            corsFilter = new CORSFilter();
            addServlet(new ServletHolder(new WebJarServlet("swagger-ui")), "/swagger/*");
            addFilter(new FilterHolder(corsFilter),"/*", EnumSet.of(DispatcherType.REQUEST));
            addServlet(new ServletHolder(new ApiServlet(List.of(
                    new LogInnController()

            ))), "/*");

            addFilter(new FilterHolder( new AuthenticationFilter(authentication)), "/*", EnumSet.of(DispatcherType.REQUEST));
        }

        public void setIsLocal(boolean isLocal) {
            corsFilter.setIsLocal(isLocal);
        }


}