package no.nav.statusplattform.api;

import no.nav.statusplattform.api.v3.controllers.AreaController;
import no.nav.statusplattform.api.v3.controllers.DashboardController;
import no.nav.statusplattform.api.v3.controllers.HealthCheck;
import no.nav.statusplattform.api.v3.controllers.OpeningHoursController;
import no.nav.statusplattform.api.v3.controllers.OpsController;
import no.nav.statusplattform.api.v3.controllers.RecordController;
import no.nav.statusplattform.api.v3.controllers.ServiceController;
import no.nav.statusplattform.api.v3.controllers.UserController;
import no.nav.statusplattform.api.v3.controllers.WcagController;
import no.nav.statusplattform.infrastructure.ApiFilter;
import no.nav.statusplattform.infrastructure.AuthenticationFilter;
import no.nav.statusplattform.infrastructure.CORSFilter;
import no.nav.statusplattform.infrastructure.ClasspathWebAppContext;
import no.nav.statusplattform.infrastructure.WebJarServlet;
import org.actioncontroller.servlet.ApiServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.fluentjdbc.DbContext;

import javax.servlet.DispatcherType;
import javax.sql.DataSource;
import java.util.EnumSet;
import java.util.List;

public class StatusplattformApi extends ClasspathWebAppContext {

    private final DbContext dbContext = new DbContext();

    private final ApiFilter filter;
    private final CORSFilter corsFilter;


    public StatusplattformApi(String context) {
        super(context, "/webapp-web");
        corsFilter = new CORSFilter();
        addFilter(new FilterHolder(corsFilter),"/*", EnumSet.of(DispatcherType.REQUEST));
        addServlet(new ServletHolder(new WebJarServlet("swagger-ui")), "/swagger/*");
        addServlet(new ServletHolder(new ApiServlet(List.of(
                new AreaController(dbContext),
                new DashboardController(dbContext),
                new ServiceController(dbContext),
                new RecordController(dbContext),
                new OpsController(dbContext),
                new OpeningHoursController(dbContext),
                new HealthCheck(dbContext),
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
