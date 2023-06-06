package no.nav.portal.rest.api;

import no.nav.portal.infrastructure.*;
import no.nav.portal.rest.api.v3.controllers.*;
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

    private final ApiFilter filter;
    private final CORSFilter corsFilter;

    public PortalRestApi(String context) {
        super(context, "/webapp-web");
        corsFilter = new CORSFilter();
        addFilter(new FilterHolder(corsFilter),"/*", EnumSet.of(DispatcherType.REQUEST));
        addServlet(new ServletHolder(new WebJarServlet("swagger-ui")), "/swagger/*");
        addServlet(new ServletHolder(new ApiServlet(List.of(
                new AreaController(dbContext),
                new DashboardController(dbContext),
                new ServiceController(dbContext, false),
                new RecordController(dbContext),
                new OpsController(dbContext),
                new OpeningHoursController(dbContext),
                new UserController(),
                new WcagController()
        ))), "/*");

        addFilter(new FilterHolder( new AuthenticationFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        filter = new ApiFilter(dbContext);
        addFilter(new FilterHolder(filter), "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    public void setDataSource(DataSource dataSource) {
        filter.setDataSource(dataSource);
    }

    public void setFrontEndLocation(String frontEndLocation){
        corsFilter.setFrontendLocation(frontEndLocation);
    }
}
