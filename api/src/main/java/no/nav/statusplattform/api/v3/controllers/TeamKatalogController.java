package no.nav.statusplattform.api.v3.controllers;

import no.nav.statusplattform.api.Helpers.OpeningHoursHelper;
import no.nav.statusplattform.api.TeamKatalogIntegrasjon.TeamKatalogKlient;
import org.actioncontroller.*;
import org.actioncontroller.json.JsonBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamKatalogController {
    @GET("/teams/search/{name}")
    @JsonBody
    public Map<UUID, String> searchTeamsByName(@PathParam("name") String name) {
        return TeamKatalogKlient.getTeamBySearch(name);
    }

    @GET("/teams/simplified/search/{name}")
    @JsonBody
    public TeamKatalogKlient.SimpleTeamInfo searchSimplifiedTeamsByName(@PathParam("name") String name) {
        return TeamKatalogKlient.getSimplifiedTeamsBySearch(name);
    }

    @GET("/teams/{id}")
    @JsonBody
    public TeamKatalogKlient.SimpleTeamInfo TeamsById(@PathParam("id") String id) {
        return TeamKatalogKlient.getTeamById(id);
    }

    @GET("/teams/check-user")
    @JsonBody
    public boolean isUserInTeam(@RequestParam("team_id") String teamId, @RequestParam("user_id") String userId) {
        // Fetch the team by name
        TeamKatalogKlient.SimpleTeamInfo team = TeamKatalogKlient.getTeamById(teamId);
        // Check if the user exists in any of the teams' member lists
        return team.getMembers().stream()
                .anyMatch(member -> member.equalsIgnoreCase(userId));
    }
}

