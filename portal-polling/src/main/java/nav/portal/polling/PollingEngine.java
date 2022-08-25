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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PollingEngine extends Thread {

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


    public void run(){
        Thread.currentThread().setUncaughtExceptionHandler(new PollingThreadExceptionHandler(dbContext,dataSource));
        try{
            startPoll();
        }
        catch (Exception e){
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),e);
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void startPoll() {
        logger.info("Starting poll ----------------");
        try (DbContextConnection ignored = dbContext.startConnection(dataSource)) {
            try (DbTransaction transaction = dbContext.ensureTransaction()) {
                getPollingServicesAndPoll();
                transaction.setComplete();
                logger.info("Polled successfully poll ----------------");
            }
        }
    }
    private void getPollingServicesAndPoll(){
        List<ServiceEntity> pollingServices = serviceRepository.retrieveServicesWithPolling();
        pollingServices.forEach(this::poll);
    }

    private void poll(ServiceEntity serviceEntity){
        try{
            LocalDateTime before = LocalDateTime.now();
            PolledServiceStatus polledServiceStatus = getPolledServiceStatus(serviceEntity);
            LocalDateTime after = LocalDateTime.now();
            Integer responseTime = calcResponseTime(before,after);
            updateRecordForService(polledServiceStatus,serviceEntity, responseTime);

        }

        catch (Exception e){

            logger.info("private void poll Exception!!: " + e);

            PolledServiceStatus polledServiceStatus =  createPolledServiceStatusForUnresponsiveEndpoint();
            updateRecordForService(polledServiceStatus,serviceEntity, 0);

        }
    }

    private PolledServiceStatus getPolledServiceStatus(ServiceEntity serviceEntity) throws IOException {
        HttpURLConnection connection = getConnectionToServicePollEndpoint(serviceEntity);
        String bodyString = readBody(connection);
        connection.disconnect();
        JsonObject jsonObject = toJson(bodyString);
        PolledServiceStatus polledServiceStatus = mapToPolledServiceStatus(jsonObject);
        return polledServiceStatus;
    }

    private Integer calcResponseTime(LocalDateTime before, LocalDateTime after) {
        Duration duration = Duration.between(after, before);
        return duration.toMillisPart();
    }



    private void updateRecordForService(PolledServiceStatus polledServiceStatus, ServiceEntity serviceEntity, Integer responseTime){

        Optional<RecordEntity> latestDiffRecord = recordRepository.getLatestRecordDiff(serviceEntity.getId());
        RecordEntity newRecord = mapToRecordEntity(polledServiceStatus, serviceEntity, responseTime);

        //Dersom ny status er forskjellig fra gammel, legges det til en status_diff
        if(latestDiffRecord.isEmpty() || !latestDiffRecord.get().getStatus().equals(newRecord.getStatus())){
            recordRepository.saveStatusDiff(newRecord);
        }
        else{
            //Hvis ikke økes teller på status
            recordRepository.increaseCountOnStatusDiff(latestDiffRecord.get());

        }
        recordRepository.save(newRecord);

        //Sletter records som er eldre enn 48 timer:
        recordRepository.deleteRecordsOlderThen48hours();
    }

    private PolledServiceStatus mapToPolledServiceStatus(JsonObject jsonObject){
        PolledServiceStatus polledServiceStatus = new PolledServiceStatus();
        polledServiceStatus.setName(jsonObject.getString("name",null));//Trengs denne?
        polledServiceStatus.setTeam(jsonObject.getString("team",null));//Trengs denne?
        polledServiceStatus.setDescrption(jsonObject.getString("description",null));
        polledServiceStatus.setLogglink(jsonObject.getString("logglink",null));
        polledServiceStatus.setStatus(ServiceStatus.valueOf(jsonObject.getString("status")));
        polledServiceStatus.setTimestamp(ZonedDateTime.now());
        return polledServiceStatus;
    }

    private RecordEntity mapToRecordEntity(PolledServiceStatus polledServiceStatus, ServiceEntity serviceEntity, Integer responseTime) {
        RecordEntity recordEntity = new RecordEntity()
                .setServiceId(serviceEntity.getId())
                .setStatus(polledServiceStatus.getStatus())
                .setDescription(polledServiceStatus.getDescrption())
                .setLogglink(polledServiceStatus.getLogglink())
                .setCreated_at(polledServiceStatus.getTimestamp())
                .setResponsetime(responseTime);
        return recordEntity;
    }

    private PolledServiceStatus createPolledServiceStatusForUnresponsiveEndpoint(){
        return new PolledServiceStatus()
                .setDescrption("Service status endpoint is not responding")
                .setStatus(ServiceStatus.ISSUE)
                .setTimestamp(ZonedDateTime.now());
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
        return content.toString();
    }



    private HttpURLConnection getConnectionToServicePollEndpoint(ServiceEntity serviceEntity) throws IOException {
        String urlString;
        switch (serviceEntity.getPolling_url()){
            case MOCK: urlString = MOCK_URL + serviceEntity.getId(); break;
            case STATUSHOLDER: urlString = STATUSHOLDER_URL + serviceEntity.getId();break;
            default: urlString = serviceEntity.getPolling_url();
        }
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con;
    }



}
