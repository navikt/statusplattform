package nav.portal.polling;

import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.fluentjdbc.DbTransaction;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

public class PollingEngine  extends Thread{
    private final ServiceRepository serviceRepository;
    private final RecordRepository recordRepository;
    private final DbContext dbContext;
    private DataSource dataSource;

    public PollingEngine(DbContext dbContext) {
        this.serviceRepository = new ServiceRepository(dbContext);
        this.recordRepository = new RecordRepository(dbContext);
        this.dbContext = dbContext;
    }

    private void startPoll() {
        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {
            try (DbTransaction transaction = dbContext.ensureTransaction()) {
                poll();
                transaction.setComplete();
            }
        }
    }
    private void poll(){
        List<ServiceEntity> pollingServices = serviceRepository.retrieveServicesWithPolling();
        System.out.println(pollingServices.stream().map(ServiceEntity::getName).collect(Collectors.toList()));

    }


    public void run(){
        Integer i = 0;
        while (true){
            try {
                System.out.println("Poller --- : " + i++);
                startPoll();
                PollingEngine.sleep(1000);
            }
            catch (InterruptedException e){
                System.out.println(e);
            }
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
