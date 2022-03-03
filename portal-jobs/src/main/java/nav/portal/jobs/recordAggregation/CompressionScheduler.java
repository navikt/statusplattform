package nav.portal.jobs.recordAggregation;

import org.fluentjdbc.DbContext;

import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CompressionScheduler {

    private final DbContext dbContext = new DbContext();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int COMPRESSION_INTERVALL_IN_HOURS = 24;
    private  RecordCompressor recordCompressor;

    public CompressionScheduler() {
        recordCompressor = new RecordCompressor(dbContext);
        recordCompressor.setDaemon(true);
        recordCompressor.setName("Compression thread");
    }

    public void setDataSource(DataSource dataSource){
        recordCompressor.setDataSource(dataSource);

    }
    public void start(){
        scheduler.scheduleWithFixedDelay(recordCompressor,0, COMPRESSION_INTERVALL_IN_HOURS,TimeUnit.HOURS);
    }




}
