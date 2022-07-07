package nav.portal.polling;

import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.RecordRepository;
import nav.portal.core.repositories.ServiceRepository;
import org.fluentjdbc.DbContext;
import org.fluentjdbc.DbContextConnection;
import org.fluentjdbc.DbTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.ToDoubleBiFunction;

public class PollingEngine  extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(PollingEngine.class);
    private static final String MOCK_URL = "https://mockservice.dev.nav.no/mock/Service/";
    private static final  String MOCK = "MOCK";
    private static final  String STATUSHOLDER = "STATUSHOLDER";
    private static final String STATUSHOLDER_URL = System.getenv("statusholder_url");
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
                getPollingServicesAndPoll();
                transaction.setComplete();
            }
        }
    }
    private void getPollingServicesAndPoll(){
        List<ServiceEntity> pollingServices = serviceRepository.retrieveServicesWithPolling();
        pollingServices.forEach(this::poll);
    }

    private void poll(ServiceEntity serviceEntity){
        try{
            logger.info("private void poll 1----------------");

            LocalDateTime before = LocalDateTime.now();
            HttpURLConnection connection = getConnectionToServicePollEndpoint(serviceEntity);
            logger.info("private void poll 2----------------");
            String bodyString = readBody(connection);
            logger.info("private void poll  3----------------");
            LocalDateTime after = LocalDateTime.now();
            Integer responseTime = calcResponseTime(before,after);
            logger.info("private void poll 4----------------");
            connection.disconnect();

            JsonObject jsonObject = toJson(bodyString);
            logger.info("private void poll 5----------------");
            boolean serviceFromServiceHolder = STATUSHOLDER.equals(serviceEntity.getPolling_url());
            PolledServiceStatus polledServiceStatus = mapToPolledServiceStatus(jsonObject, serviceFromServiceHolder);
            logger.info("private void poll 6----------------");
            updateRecordForService(polledServiceStatus,serviceEntity, responseTime);

        }
        catch (Exception e){

            logger.info("private void poll Exception!!: " + e);

            PolledServiceStatus polledServiceStatus =  createPolledServiceStatusForUnresponsiveEndpoint();
            updateRecordForService(polledServiceStatus,serviceEntity, 0);

        }


    }

    private Integer calcResponseTime(LocalDateTime before, LocalDateTime after) {
        Duration duration = Duration.between(after, before);
        return duration.toMillisPart();
    }

    private PolledServiceStatus mapToPolledServiceStatus(JsonObject jsonObject,Boolean statusFromStatusholder){
        PolledServiceStatus polledServiceStatus = new PolledServiceStatus();
        polledServiceStatus.setName(jsonObject.getString("name",null));//Trengs denne?
        polledServiceStatus.setTeam(jsonObject.getString("team",null));//Trengs denne?
        polledServiceStatus.setDescrption(jsonObject.getString("description",null));
        polledServiceStatus.setLogglink(jsonObject.getString("logglink",null));
        polledServiceStatus.setStatus(ServiceStatus.valueOf(jsonObject.getString("status")));
        //TODO FINN UT OM DETTE ER RIKTIG:
        polledServiceStatus.setTimestamp(ZonedDateTime.now());

        return polledServiceStatus;
    }

    private void updateRecordForService(PolledServiceStatus polledServiceStatus, ServiceEntity serviceEntity, Integer responseTime){
        RecordEntity recordEntity = new RecordEntity()
                .setServiceId(serviceEntity.getId())
                .setStatus(polledServiceStatus.getStatus())
                .setDescription(polledServiceStatus.getDescrption())
                .setLogglink(polledServiceStatus.getLogglink())
                .setCreated_at(polledServiceStatus.getTimestamp())
                .setResponsetime(responseTime);
        recordRepository.save(recordEntity);
    }

    private PolledServiceStatus createPolledServiceStatusForUnresponsiveEndpoint(){
        PolledServiceStatus polledServiceStatus = new PolledServiceStatus()
                .setDescrption("Service status endpoint is not responding")
                .setStatus(ServiceStatus.ISSUE)
                .setTimestamp(ZonedDateTime.now());
        return polledServiceStatus;
    }

    private ZonedDateTime readDateTimeFromString(String str){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS zzz");
        return ZonedDateTime.from(formatter.parse(str));

    }

    private static JsonObject toJson(String str){
        JsonReader jsonReader = Json.createReader(new StringReader(str));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    private static String readBody(HttpURLConnection con) throws IOException {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        String result = content.toString();
        logger.info("In read body ..........");
        logger.info("result: "+ result);
        return result;
    }



    private HttpURLConnection getConnectionToServicePollEndpoint(ServiceEntity serviceEntity) throws IOException {
        //Logikken under må bort på et tidspunkt. Dette er for polling av mock data.
        String urlString;
        switch (serviceEntity.getPolling_url()){
            case MOCK: urlString = MOCK_URL + serviceEntity.getId(); break;
            case STATUSHOLDER: urlString = STATUSHOLDER_URL + serviceEntity.getId();break;
            default: urlString = serviceEntity.getPolling_url();
        }
        logger.info("In poller ..........");
        logger.info("url: "+ urlString);
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con;
    }


    public void run(){
        startPoll();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
