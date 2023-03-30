package no.nav.portal.rest.api.Helpers;

import no.portal.web.generated.api.ServiceDto;
import org.actioncontroller.HttpRequestException;
import org.apache.commons.validator.routines.UrlValidator;

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

    private static final  String STATUSHOLDER = "STATUSHOLDER";
    private static final String STATUSHOLDER_URL = System.getenv("statusholder_url");
    private static final String STATUSHOLDER_ENDPOINTCECK = STATUSHOLDER_URL+ "/checkSatusEndpoint/?url=";

    public static boolean validateAndIsOnPrem(ServiceDto serviceDto){
        //Throws error if url not valid, or if url not responding
        //Returns false if endpoint is on gcp, returns true if endpoint onprem
        validateUrl(serviceDto.getPollingUrl());
        if(checkIfEndpointRespondsGcp(serviceDto)){
            return false;
        }
        if(checkIfEndpointRespondsOnPrem(serviceDto)){
            return true;
        }
        else {
            throw new HttpRequestException("PollingUrl not responding: "+ serviceDto.getPollingUrl());
        }



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
    public static Boolean checkIfEndpointRespondsGcp(ServiceDto serviceDto){
        try {
            HttpURLConnection connection = getConnectionToGcpEndpoint(serviceDto);

            String bodyString = readBody(connection);
            connection.disconnect();
            JsonObject jsonObject = toJson(bodyString);
            return checkForStatus(jsonObject);

        }
        catch (IOException e){
            return false;
        }
    }

    public static Boolean checkIfEndpointRespondsOnPrem(ServiceDto serviceDto){
        try {
            HttpURLConnection connection = getConnectionToOnpremEndpoint(serviceDto);

            String bodyString = readBody(connection);
            connection.disconnect();
            JsonObject jsonObject = toJson(bodyString);
            return checkForStatus(jsonObject);

        }
        catch (IOException e){
            return false;
        }
    }




    private static HttpURLConnection getConnectionToOnpremEndpoint(ServiceDto serviceDto) throws IOException {
        String urlString = STATUSHOLDER_ENDPOINTCECK + serviceDto.getPollingUrl();
        return getConnectionEndpoint(urlString);
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
        return jsonRecord.getString("status") != null && !jsonRecord.getString("status").equals("");

    }
}
