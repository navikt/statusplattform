package no.nav.portal.rest.api.v3.controllers;

import no.nav.portal.infrastructure.PortalRestPrincipal;
import no.portal.web.generated.api.NavUserDto;
import org.actioncontroller.GET;
import org.actioncontroller.UserPrincipal;
import org.actioncontroller.json.JsonBody;

import java.util.Optional;

public class UserController {
    @GET("/NavUser")
    @JsonBody
    public NavUserDto logInnNavUser(@UserPrincipal Optional<PortalRestPrincipal> portalRestPrincipal){
        return portalRestPrincipal
                .map(loggedInUser -> new NavUserDto()
                        .name(loggedInUser.getName())
                        .navIdent(loggedInUser.getNavIdent())
                )
                .orElse(new NavUserDto());
    }
}
