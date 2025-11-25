package no.nav.statusplattform.api.v3.controllers;

import nav.statusplattform.core.entities.ServiceEntity;
import nav.statusplattform.core.repositories.ServiceRepository;
import no.nav.statusplattform.api.EntityDtoMappers;
import no.nav.statusplattform.api.Helpers.ServiceControllerHelper;
import no.nav.statusplattform.api.Helpers.StatusUrlValidator;
import no.nav.statusplattform.generated.api.AreaDto;
import no.nav.statusplattform.generated.api.MaintenanceDto;
import no.nav.statusplattform.generated.api.ServiceDto;
import no.nav.statusplattform.generated.api.ServiceTypeDto;
import no.nav.statusplattform.generated.api.StatusDto;
import org.actioncontroller.DELETE;
import org.actioncontroller.GET;
import org.actioncontroller.HttpRequestException;
import org.actioncontroller.POST;
import org.actioncontroller.PUT;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;
import org.jsonbuddy.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServiceController {

    private final ServiceControllerHelper serviceControllerHelper;
    private final ServiceRepository serviceRepository;
    // TODO: Replace w/AppConfig
    private String STATUSHOLDER_URL = System.getenv("statusholder_url");
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    public ServiceController(DbContext dbContext) {
        this.serviceControllerHelper = new ServiceControllerHelper(dbContext);
        this.serviceRepository = new ServiceRepository(dbContext);
    }


    @GET("/Services/PollingServices")
    @JsonBody
    public  List<ServiceDto> getPollingServices() {
        return serviceControllerHelper.getPollingServices();
    }

    @GET("/Services/PollingServicesOnPrem")
    @JsonBody
    public  List<ServiceDto> getPollingServicesOnPrem() {
        return serviceControllerHelper.retrieveServicesWithPollingOnPrem();
    }

    @GET("/Services/Minimal")
    @JsonBody
    public  List<ServiceDto> getServicesMinimal() {
        return serviceControllerHelper.getAllServicesShallow();
    }

    @GET("/Components/Minimal")
    @JsonBody
    public  List<ServiceDto> getComponentsMinimal() {
        return serviceControllerHelper.getAllComponentsShallow();
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

    @POST("/Service")
    @JsonBody
    public ServiceDto newService(@JsonBody ServiceDto serviceDto) {
        if(StatusUrlValidator.validateUrl(serviceDto.getPollingUrl())){
            return serviceControllerHelper.saveNewService(serviceDto);

        }
        throw new HttpRequestException("Polling not valid: "+ serviceDto.getPollingUrl());
    }


    @PUT("/Service/:Service_id")
    @JsonBody
    public void updateService(@PathParam("Service_id") UUID service_id, @JsonBody ServiceDto serviceDto) {
        if(StatusUrlValidator.validateUrl(serviceDto.getPollingUrl())){
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

    @GET("/services/external")
    @JsonBody
    public List<ServiceDto> getAllExternalServices() {
        List<ServiceEntity> services = serviceRepository.getAllExternalServices(); // Or call repository directly if no service layer
        return services.stream()
                .map(EntityDtoMappers::toServiceDtoShallow) // Assuming `toServiceDto` maps `ServiceEntity` to `ServiceDto`
                .collect(Collectors.toList());
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
        return serviceControllerHelper.getAllStatusesFromStatusholder(body);
    }

    private HttpURLConnection getAllStatusesFromStatusholderConnection() throws IOException {
        URL url = new URL(STATUSHOLDER_URL+"/status");
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

        URL url = new URL(STATUSHOLDER_URL+ "/status/");
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
