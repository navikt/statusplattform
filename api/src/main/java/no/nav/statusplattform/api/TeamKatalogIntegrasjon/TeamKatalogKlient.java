package no.nav.statusplattform.api.TeamKatalogIntegrasjon;

import jakarta.validation.constraints.Null;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class TeamKatalogKlient {
    private static final Logger logger = LoggerFactory.getLogger(TeamKatalogKlient.class);
    private static final String TEAM_API_SUFFIX = "/team?status=ACTIVE%2CPLANNED%2CINACTIVE";
    private static final String TEAM_KATALOG_URL = System.getenv("teamkatalogApiUrl");
    private static String teamkatalogApiUrl = System.getenv("teamkatalogApiUrl") + TEAM_API_SUFFIX;

    private static final Map<UUID, String> teamIdName = new HashMap<>();
    private static LocalDate lastUpdate;
    private static boolean shouldUpdate = true;

    public static Map<UUID, String> getTeams() {
        if (teamIdName.isEmpty() || LocalDate.now().isAfter(lastUpdate) || shouldUpdate) {
            refreshTeams();
        }
        shouldUpdate = !shouldUpdate;
        return new HashMap<>(teamIdName);
    }

    public static void updateTeams() {
        refreshTeams();
    }

    public static SimpleTeamInfo getSimplifiedTeamsBySearch(String searchParam) {
        logger.info("Searching for teams with the search parameter: {}", searchParam);
        String searchUrl = buildSearchUrl(searchParam);
        return fetchSimpleTeamsFromUrl(searchUrl, false);
    }

    private static SimpleTeamInfo fetchSimpleTeamsFromUrl(String urlString, Boolean single_team ) {
        SimpleTeamInfo simpleTeams;
        try {
            HttpURLConnection connection = createApiConnection(urlString);
            logger.info("UrlString: {}", urlString);
            String responseBody = readResponseBody(connection);

            if(single_team){
                simpleTeams = parseSimpleSingleTeamFromJson(responseBody);
            }else {
                simpleTeams = parseSimpleTeamsFromJson(responseBody);
            }
            logger.info("Successfully fetched simplified teams from TeamKatalog API");
            return simpleTeams;

        } catch (Exception e) {
            logger.error("Error while fetching simplified teams from TeamKatalog API", e);
        }
        return null;
    }

    public static Map<UUID, String> getTeamBySearch(String searchParam) {
        logger.info("Searching for teams with the search parameter: {}", searchParam);
        String searchUrl = buildSearchUrl(searchParam);
        return fetchTeamsFromUrl(searchUrl);
    }

    public static SimpleTeamInfo getTeamById(String team_id) {
        logger.info("Fetching team with id: {}", team_id);
        String URL = TEAM_KATALOG_URL + "/team/" + team_id;
        return fetchSimpleTeamsFromUrl(URL, true);
    }

    private static void refreshTeams() {
        logger.info("Refreshing teams from TeamKatalog API");
        teamIdName.clear();
        teamIdName.putAll(fetchTeamsFromUrl(teamkatalogApiUrl));
        lastUpdate = LocalDate.now();
    }

    private static Map<UUID, String> fetchTeamsFromUrl(String urlString) {
        Map<UUID, String> teams = new HashMap<>();
        try {
            HttpURLConnection connection = createApiConnection(urlString);
            String responseBody = readResponseBody(connection);

            if (responseBody == null || responseBody.isEmpty()) {
                logger.warn("Received empty response from TeamKatalog API");
                return teams;
            }

            // Bruk gammel parse-metode for eksisterende logikk
            teams = parseLegacyTeamsFromJson(responseBody);
            logger.info("Successfully fetched and parsed teams from TeamKatalog API");

        } catch (IOException e) {
            logger.error("I/O error occurred while fetching teams from TeamKatalog API", e);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching teams from TeamKatalog API", e);
        }

        return teams;
    }

    private static String buildSearchUrl(String searchParam) {
        if (searchParam == null || searchParam.isEmpty()) {
            logger.warn("Empty search parameter provided");
            return teamkatalogApiUrl;
        }

        String encodedParam = URLEncoder.encode(searchParam, StandardCharsets.UTF_8).replace("+", "%20");
        return TEAM_KATALOG_URL + "/team/search/" + encodedParam;
    }

    private static HttpURLConnection createApiConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }

    private static String readResponseBody(HttpURLConnection connection) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
    }

    private static Map<UUID, String> parseLegacyTeamsFromJson(String responseBody) throws Exception {
        Map<UUID, String> teams = new HashMap<>();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseBody);
        JSONArray content = (JSONArray) json.get("content");

        for (Object object : content) {
            JSONObject team = (JSONObject) object;
            UUID id = UUID.fromString((String) team.get("id"));
            String name = (String) team.get("name");
            teams.put(id, name);
        }
        return teams;
    }

    private static SimpleTeamInfo parseSimpleTeamsFromJson(String responseBody) throws Exception {
        List<SimpleTeamInfo> teams = new ArrayList<>();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseBody);
        JSONArray content = (JSONArray) json.get("content");

        for (Object object : content) {
            JSONObject team = (JSONObject) object;
            UUID id = UUID.fromString((String) team.get("id"));
            String name = (String) team.get("name");

            // Fetch member information as a list of strings
            JSONArray membersArray = (JSONArray) team.get("members");
            List<String> members = new ArrayList<>();
            for (Object memberObject : membersArray) {
                JSONObject memberJson = (JSONObject) memberObject;
                String navIdent = (String) memberJson.get("navIdent");
                members.add(navIdent);
            }

            teams.add(new SimpleTeamInfo(id, name, members));
        }
        return teams.getFirst();
    }

    private static SimpleTeamInfo parseSimpleSingleTeamFromJson(String responseBody) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject team = (JSONObject) parser.parse(responseBody);

        UUID id = UUID.fromString((String) team.get("id"));
        String name = (String) team.get("name");

        // Fetch member information as a list of strings
        JSONArray membersArray = (JSONArray) team.get("members");
        List<String> members = new ArrayList<>();
        for (Object memberObject : membersArray) {
            JSONObject memberJson = (JSONObject) memberObject;
            String navIdent = (String) memberJson.get("navIdent");
            members.add(navIdent);
        }

        return new SimpleTeamInfo(id, name, members);
    }

    // Indre klasse for enkel teaminformasjon
    public static class SimpleTeamInfo {
        private UUID id;
        private String name;
        private List<String> members;

        public SimpleTeamInfo(UUID id, String name, List<String> members) {
            this.id = id;
            this.name = name;
            this.members = members;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getMembers() {
            return members;
        }

        public void setMembers(List<String> members) {
            this.members = members;
        }
    }
}