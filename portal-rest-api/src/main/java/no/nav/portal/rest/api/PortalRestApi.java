package no.nav.portal.rest.api;

import no.nav.portal.infrastructure.ApiFilter;
import no.nav.portal.infrastructure.AuthenticationFilter;
import no.nav.portal.infrastructure.ClasspathWebAppContext;
import no.nav.portal.infrastructure.WebJarServlet;
import no.nav.portal.rest.api.v3.controllers.AreaController;
import org.actioncontroller.servlet.ApiServlet;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.fluentjdbc.DbContext;

import javax.servlet.DispatcherType;
import javax.sql.DataSource;
import java.util.EnumSet;
import java.util.List;

public class PortalRestApi extends ClasspathWebAppContext {

    private final DbContext dbContext = new DbContext();
    private final Authentication authentication = new Authentication() {
        @Override
        public int hashCode() {
            return super.hashCode();
        }
    };

    private final ApiFilter filter;

    public PortalRestApi(String context) {
        super(context, "/webapp-web");
        addServlet(new ServletHolder(new WebJarServlet("swagger-ui")), "/swagger/*");
        addServlet(new ServletHolder(new ApiServlet(List.of(
                new AreaController(dbContext)

        ))), "/v0.1/*");

        addFilter(new FilterHolder( new AuthenticationFilter(authentication)), "/*", EnumSet.of(DispatcherType.REQUEST));
        filter = new ApiFilter(dbContext);
        addFilter(new FilterHolder(filter), "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    public void setDataSource(DataSource dataSource) {
        filter.setDataSource(dataSource);
    }

}
