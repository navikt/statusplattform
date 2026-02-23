package no.nav.statusplattform.api;

import no.nav.statusplattform.api.Helpers.EmailService;
import no.nav.statusplattform.api.v3.controllers.*;
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
    private final EmailService emailService;


    public StatusplattformApi(String context) {
        super(context, "/webapp-web");
        corsFilter = new CORSFilter();
        addFilter(new FilterHolder(corsFilter),"/*", EnumSet.of(DispatcherType.REQUEST));
        addServlet(new ServletHolder(new WebJarServlet("swagger-ui")), "/swagger/*");
        var dbContext = new DbContext();
        emailService = new EmailService();
        addServlet(new ServletHolder(new ApiServlet(List.of(
                new AreaController(dbContext),
                new DashboardController(dbContext),
                new ServiceController(dbContext),
                new RecordController(dbContext),
                new OpsController(dbContext),
                new OpeningHoursController(dbContext),
                new HealthCheck(dbContext),
                new UserController(),
                new WcagController(),
                new UpTimeController(dbContext),
                new TeamKatalogController(),
                new SubscriptionController(dbContext, emailService)
        ))), "/*");

        addFilter(new FilterHolder( new AuthenticationFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        filter = new ApiFilter(dbContext);
        addFilter(new FilterHolder(filter), "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setDataSource(DataSource dataSource) {
        filter.setDataSource(dataSource);
    }

    public void setFrontEndLocation(String frontEndLocation){
        corsFilter.setFrontendLocation(frontEndLocation);
    }
}
