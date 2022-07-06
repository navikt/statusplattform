package no.nav.portal.rest.api.v3.controllers;


import nav.portal.core.entities.RecordEntity;
import nav.portal.core.entities.ServiceEntity;
import nav.portal.core.enums.ServiceStatus;
import nav.portal.core.repositories.ServiceRepository;
import no.nav.portal.rest.api.EntityDtoMappers;
import no.nav.portal.rest.api.Helpers.ServiceControllerHelper;
import no.nav.portal.rest.api.Helpers.Util;
import no.portal.web.generated.api.*;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;
import org.jsonbuddy.JsonObject;


import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class ServiceController {

    private final ServiceControllerHelper serviceControllerHelper;
    private final ServiceRepository serviceRepository;
    private String STATUSHOLDER_URL = System.getenv("statusholder_url");


    public ServiceController(DbContext dbContext) {
        this.serviceControllerHelper = new ServiceControllerHelper(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
    }

    @GET("/Services")
    @JsonBody
    public  List<ServiceDto> getServices() {
        return serviceControllerHelper.getAllServices();
    }

    @GET("/Components")
    @JsonBody
    public  List<ServiceDto> getComponents() {
        return serviceControllerHelper.getAllComponents();
    }


    @DELETE("/Component/:Service_id")
    @JsonBody
    public void deleteComponent(@PathParam("Service_id") UUID componentId) {
        serviceControllerHelper.deleteComponent(componentId);
    }

    @GET("/Service/:Service_id")
    @JsonBody
    public ServiceDto getService(@PathParam("Service_id") UUID service_id) {
        return serviceControllerHelper.retrieveOneService(service_id);
    }

    @GET("/Service/HistoryAggregated/:Service_id")
    @JsonBody
    public ServiceHistoryDto getServiceHistoryTwelveMonthsBack(@PathParam("Service_id") UUID service_id) {
        return serviceControllerHelper.getServiceHistoryForTwelveMonths(service_id, 12);
    }

    @POST("/Service")
    @JsonBody
    public ServiceDto newService(@JsonBody ServiceDto serviceDto) {
        if(Util.validateUrl(serviceDto.getPollingUrl())){
            return serviceControllerHelper.saveNewService(serviceDto);

        }
        throw new HttpRequestException("Polling not valid: "+ serviceDto.getPollingUrl());
    }

    @PUT("/Service/:Service_id")
    @JsonBody
    public void updateService(@PathParam("Service_id") UUID service_id, @JsonBody ServiceDto serviceDto) {
        if(Util.validateUrl(serviceDto.getPollingUrl())){
            serviceDto.setId(service_id);
            serviceControllerHelper.updateService(serviceDto);
            return;
        }
        throw new HttpRequestException("Polling not valid: "+ serviceDto.getPollingUrl());

    }

    @PUT("/Service/:Service_id/:DependentOnService_id")
    @JsonBody
    public void addDependencyToService(@PathParam("Service_id") UUID service_id
            ,@PathParam("DependentOnService_id") UUID dependentOnService_id) {
        serviceRepository.addDependencyToService(service_id,dependentOnService_id);
    }

    @DELETE("/Service/:Service_id/:DependentOnService_id")
    public void removeDependencyFromService(@PathParam("Service_id") UUID service_id
            ,@PathParam("DependentOnService_id") UUID dependentOnService_id) {
        serviceRepository.removeDependencyFromService(service_id,dependentOnService_id);
    }



    @DELETE("/Service/:Service_id")
    @JsonBody
    public void deleteService(@PathParam("Service_id") UUID service_id) {
        serviceControllerHelper.deleteService(service_id);
    }


    @PUT("/Service/Maintenance")
    @JsonBody
    public void addMaintenance(@JsonBody MaintenanceDto maintenanceDto) {
        serviceRepository.saveMaintenance(EntityDtoMappers.toMaintenanceEntity(maintenanceDto));
    }

    @GET("/Service/Maintenance/:Service_id")
    @JsonBody
    public List<MaintenanceDto> addMaintenance(@PathParam("Service_id") UUID service_id) {
        return serviceRepository.getMaintenanceForService(service_id).stream().map(EntityDtoMappers::toMaintenanceDto).collect(Collectors.toList());
    }


    @GET("/Service/Areas/:Service_id")
    @JsonBody
    public List<AreaDto> getAreasContainingService(@PathParam("Service_id") UUID service_id) {

        return serviceControllerHelper.getAreasContainingService(service_id);
    }


    @GET("/Services/Types")
    @JsonBody
    public List<String> getServicetypes() {
        return ServiceTypeDto.getValues();
    }

    @GET("/Services/Status")
    @JsonBody
    public List<String> getServiceStatuses() {
        return StatusDto.getValues();
    }


    @GET("/Statusholder")
    @JsonBody
    public List<JsonObject> getStatusHolderStatuses() throws IOException  {
        try{
            return getAllStatusesFromStatusholder();
        }
        catch (IOException e){
            return toJson("'error':'couldNotReadFromStatusholder'");
        }
    }

    @POST("/Statusholder/:Service_id/:Status")
    @JsonBody
    public int addStatusToStatusholder(@PathParam("Service_id") UUID service_id
            ,@PathParam("Status") String status) {
        try{
            return putToStatusholder(service_id,status);
        }
        catch (IOException e){
            return 666;
        }

    }


    private static List<JsonObject> toJson(String str){
        try{
            JsonReader jsonReader = Json.createReader(new StringReader(str));
            JsonObject object = (JsonObject) jsonReader.readObject();
            jsonReader.close();
            return List.of(object);

        }catch (javax.json.JsonException e){
            try {
                JsonReader jsonReader  =  Json.createReader(new StringReader(str));
                JsonArray jsonArray = jsonReader.readArray();
                return jsonArray.stream().map(jsonValue -> JsonObject.parse(jsonValue.toString())).collect(Collectors.toList());
            }
            catch (Exception e2){
                return List.of(new JsonObject());
            }
        }
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


    private List<JsonObject> getAllStatusesFromStatusholder() throws IOException  {
        HttpURLConnection con = getAllStatusesFromStatusholderConnection();
        String stringBody = readBody(con);
        List<JsonObject> body = toJson(stringBody);
        return body;
    }




    private HttpURLConnection getAllStatusesFromStatusholderConnection() throws IOException {
        URL url = new URL(STATUSHOLDER_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con;
    }





    private int putToStatusholder(UUID id, String status) throws IOException {
        HttpURLConnection con = putToStatusholderConnection(id, status);
        return con.getResponseCode();

    }




    private HttpURLConnection putToStatusholderConnection(UUID id, String status) throws IOException {
        //Logikken under må bort på et tidspunkt. Dette er for polling av mock data.



        URL url = new URL(STATUSHOLDER_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");
        String jsonInputString = "{'serviceId':"+id+", 'status': "+status+"}";
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return con;
    }


}
