package no.nav.portal.rest.api.TeamKatalogIntegrasjon;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamKatalogKlient {
    private static final Logger logger = LoggerFactory.getLogger(TeamKatalogKlient.class);
    private static String teamkatalogApiUrl = System.getenv("teamkatalogApiUrl") + "/team?status=ACTIVE%2CPLANNED%2CINACTIVE";
    static Map<UUID,String> teamIdName = new HashMap<>();
    private static LocalDate lastUpdate;
    private static Boolean shouldUpdate = true;


    static {
        getTeamsFromTeamkatalog();
    }


    public static Map<UUID,String> getTeams(){
        if(teamIdName.isEmpty() || LocalDate.now().isAfter(lastUpdate)|| shouldUpdate){
            getTeamsFromTeamkatalog();
        }
        shouldUpdate = !shouldUpdate;
        return teamIdName;
    }

    public static void updateTeams(){
        getTeamsFromTeamkatalog();

    }

    private static void getTeamsFromTeamkatalog() {
        logger.info("In teamkatalog klient");
        HttpURLConnection connection;
        try {
            connection = getApiConnection();
            String body = readBody(connection);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(body);
            JSONArray content = (JSONArray) json.get("content");
            content.forEach( object -> {
                JSONObject team = (JSONObject) object;
                String name = (String) team.get("name");
                UUID uuid = UUID.fromString((String) team.get("id"));
                teamIdName.put(uuid, name);
            });
            logger.info("succesfully loaded teams for teamkatalog");
        }
        catch (Exception e){
            logger.info("Error reaching team api");
            logger.info(e.getMessage());

        }
        lastUpdate = LocalDate.now();
    }



    private static HttpURLConnection getApiConnection() throws IOException {
        String urlString = teamkatalogApiUrl + "";
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


}
