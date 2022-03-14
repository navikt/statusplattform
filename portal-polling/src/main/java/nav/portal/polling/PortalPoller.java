package nav.portal.polling;


import org.fluentjdbc.DbContext;

import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PortalPoller  {

    private final DbContext dbContext = new DbContext();
    private PollingEngine pollingEngine;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int POLLING_INTERVAL_IN_SECONDS = 60;

    public PortalPoller() {
        pollingEngine = new PollingEngine(dbContext);
        pollingEngine.setDaemon(true);
        pollingEngine.setName("Polling thread");
    }

    public void setDataSource(DataSource dataSource){
        pollingEngine.setDataSource(dataSource);

    }
    public void start(){
        scheduler.scheduleWithFixedDelay(pollingEngine,0,POLLING_INTERVAL_IN_SECONDS,TimeUnit.SECONDS);

    }




}
