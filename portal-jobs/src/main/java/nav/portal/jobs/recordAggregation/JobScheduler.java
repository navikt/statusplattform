package nav.portal.jobs.recordAggregation;

import org.fluentjdbc.DbContext;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobScheduler {

    private final DbContext dbContext = new DbContext();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int COMPRESSION_INTERVALL_IN_SECONDS= 24*60*60;
    private final RecordCompressor recordCompressor;


    public JobScheduler() {
        recordCompressor = new RecordCompressor(dbContext);
        recordCompressor.setDaemon(true);
        recordCompressor.setName("Compression thread");
    }

    public void setDataSource(DataSource dataSource){
        recordCompressor.setDataSource(dataSource);

    }
    public void start(){
        int secoundsInAday =  24*60*60;
        int initialDelay = secoundsInAday - LocalTime.now().toSecondOfDay();
        scheduler.scheduleWithFixedDelay(recordCompressor,initialDelay, COMPRESSION_INTERVALL_IN_SECONDS,TimeUnit.SECONDS);
    }




}
