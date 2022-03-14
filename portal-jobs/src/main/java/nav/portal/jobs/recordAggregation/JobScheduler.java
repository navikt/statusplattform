package nav.portal.jobs.recordAggregation;

import org.fluentjdbc.DbContext;

import javax.sql.DataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobScheduler {

    private final DbContext dbContext = new DbContext();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int COMPRESSION_INTERVALL_IN_HOURS = 24;
    private final RecordCompressor recordCompressor;
    private DataSource dataSource;


    public JobScheduler() {
        recordCompressor = new RecordCompressor(dbContext);
        recordCompressor.setDaemon(true);
        recordCompressor.setName("Compression thread");
    }

    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
        recordCompressor.setDataSource(dataSource);

    }
    public void start(){
        //MockDataGenerator.generateRandomStatusesForAllServices(dbContext, dataSource);
        //scheduler.scheduleWithFixedDelay(recordCompressor,0, COMPRESSION_INTERVALL_IN_HOURS,TimeUnit.HOURS);
    }




}
