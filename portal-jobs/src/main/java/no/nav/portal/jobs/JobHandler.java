package no.nav.portal.jobs;
import org.fluentjdbc.DbContext;

import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobHandler {

    private final DbContext dbContext = new DbContext();
    private OpeningHoursDailyEval openingHoursDailyEval;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int UPDATE_TIME = 24;

    public JobHandler() {
        openingHoursDailyEval = new OpeningHoursDailyEval(dbContext);
        openingHoursDailyEval.setDaemon(true);
        openingHoursDailyEval.setName("Polling thread");
    }

    public void setDataSource(DataSource dataSource){
        openingHoursDailyEval.setDataSource(dataSource);

    }
    public void start(){
        scheduler.scheduleWithFixedDelay(openingHoursDailyEval,0, UPDATE_TIME ,TimeUnit.HOURS);

    }




}
