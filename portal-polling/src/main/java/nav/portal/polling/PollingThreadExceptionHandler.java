package nav.portal.polling;

import org.fluentjdbc.DbContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingThreadExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private static final Logger logger = LoggerFactory.getLogger(PollingThreadExceptionHandler.class);
    private final DbContext dbContext;

    public PollingThreadExceptionHandler(DbContext dbContext){
        this.dbContext = dbContext;
    }

    public void uncaughtException(Thread t, Throwable e)
    {
        System.out.printf("An exception has been captured\n");
        System.out.printf("Thread: %s\n", t.getId());
        System.out.printf("Exception: %s: %s\n", e.getClass().getName(), e.getMessage());
        System.out.printf("Stack Trace: \n");
        e.printStackTrace(System.out);
        System.out.printf("Thread status: %s\n", t.getState());
        new Thread(new PollingEngine(dbContext)).start();
    }

}
