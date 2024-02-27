package no.nav.statusplattform.infrastructure;


import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.fluentjdbc.DbTransaction;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

public class ApiFilter implements Filter {
    private final DbContext dbContext;
    private DataSource dataSource;

    public ApiFilter(DbContext dbContext) {
        this.dbContext = dbContext;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {
            try (DbTransaction transaction = dbContext.ensureTransaction()) {
                chain.doFilter(request, response);
                transaction.setComplete();
            }
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }
}
