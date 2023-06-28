package no.nav.portal.rest.api.Helpers;

import no.portal.web.generated.api.RecordDto;
import no.portal.web.generated.api.ServiceDto;
import org.actioncontroller.HttpRequestException;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StatusUrlValidator {

    private static final Logger logger = LoggerFactory.getLogger(StatusUrlValidator.class);

    private static final  String STATUSHOLDER = "STATUSHOLDER";
    private static final String STATUSHOLDER_URL = System.getenv("statusholder_url");
    private static final String STATUSHOLDER_ENDPOINTCECK = STATUSHOLDER_URL+ "/checkSatusEndpoint/?url=";

    public static boolean validateAndIsOnPrem(ServiceDto serviceDto){
        //Throws error if url not valid, or if url not responding
        //Returns false if endpoint is on gcp, returns true if endpoint onprem
        validateUrl(serviceDto.getPollingUrl());
        if(checkIfEndpointRespondsOnPrem(serviceDto)){
            return true;
        }
        return false;
    }

    public static Boolean validateUrl(String pollingUrl){
        if(pollingUrl == null || pollingUrl.equals("")){
            return true;
        }
        String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (urlValidator.isValid(pollingUrl) || STATUSHOLDER.equals(pollingUrl)) {
            return true;
        } else {
            throw new HttpRequestException("Polling not valid: "+ pollingUrl);
        }

    }

    private static Boolean checkIfEndpointRespondsGcp(ServiceDto serviceDto){
        if(serviceDto.getPollingUrl().equals(STATUSHOLDER)) {
            return true;
        }
        JsonObject jsonObject = getEndpointRespondFromGcp(serviceDto.getPollingUrl());
        return checkForStatus(jsonObject);
    }

    private static JsonObject getEndpointRespondFromGcp(String urlString){
        try {
            HttpURLConnection connection = getConnectionEndpoint(urlString);
            String bodyString = readBody(connection);
            connection.disconnect();
            return toJson(bodyString);
        }
        catch (IOException e){
            return null;
        }
    }

    private static boolean checkIfEndpointRespondsOnPrem(ServiceDto serviceDto){
        return checkForStatus(getEndpointRespondFromOnePrem(serviceDto.getPollingUrl()));
    }
    private static JsonObject getEndpointRespondFromOnePrem(String urlString){
        try {
            HttpURLConnection connection = getConnectionToOnpremEndpoint(urlString);
            String bodyString = readBody(connection);
            connection.disconnect();
            return toJson(bodyString);

        }
        catch (IOException e){
            return null;
        }
    }





    private static HttpURLConnection getConnectionToOnpremEndpoint(String endUrl) throws IOException {
        String fullUrlString = STATUSHOLDER_ENDPOINTCECK + endUrl;
        return getConnectionEndpoint(fullUrlString);
    }

    private static HttpURLConnection getConnectionToGcpEndpoint(ServiceDto serviceDto) throws IOException {
        String urlString;
        if(serviceDto.getPollingUrl().equals(STATUSHOLDER)){
            urlString = STATUSHOLDER_URL+"/status/" + serviceDto.getId();
        }
        else {
            urlString = serviceDto.getPollingUrl();
        }
        return getConnectionEndpoint(urlString);
    }

    private static HttpURLConnection getConnectionEndpoint(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con;
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
    private static JsonObject toJson(String str){
        JsonReader jsonReader = Json.createReader(new StringReader(str));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    private static Boolean checkForStatus(JsonObject jsonRecord){
        if(jsonRecord == null) {
            return false;
        }
        return jsonRecord.getString("status") != null && !jsonRecord.getString("status").equals("");

    }
}
