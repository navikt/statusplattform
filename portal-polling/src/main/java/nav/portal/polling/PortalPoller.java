package nav.portal.polling;


import org.fluentjdbc.DbContext;

import javax.sql.DataSource;

public class PortalPoller  {

    private final DbContext dbContext = new DbContext();
    private PollingEngine pollingEngine;

    public PortalPoller() {
        pollingEngine = new PollingEngine(dbContext);
    }

    public void setDataSource(DataSource dataSource){
        pollingEngine.setDataSource(dataSource);

    }
    public void start(){
        pollingEngine.start();
    }




}
