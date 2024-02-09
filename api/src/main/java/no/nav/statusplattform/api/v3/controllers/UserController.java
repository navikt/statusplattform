package no.nav.statusplattform.api.v3.controllers;

import no.nav.statusplattform.generated.api.NavUserDto;
import no.nav.statusplattform.infrastructure.StatusplattformRestPrincipal;
import org.actioncontroller.GET;
import org.actioncontroller.UserPrincipal;
import org.actioncontroller.json.JsonBody;

import java.util.Optional;

public class UserController {
    @GET("/NavUser")
    @JsonBody
    public NavUserDto logInnNavUser(@UserPrincipal Optional<StatusplattformRestPrincipal> portalRestPrincipal){
        return portalRestPrincipal
                .map(loggedInUser -> new NavUserDto()
                        .name(loggedInUser.getName())
                        .navIdent(loggedInUser.getNavIdent())
                )
                .orElse(new NavUserDto());
    }
}
