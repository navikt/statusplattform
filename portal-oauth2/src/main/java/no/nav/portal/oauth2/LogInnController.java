package no.nav.portal.oauth2;

import no.nav.portal.infrastructure.PortalRestPrincipal;
import no.portal.web.generated.api.NavUserDto;
import org.actioncontroller.GET;
import org.actioncontroller.UserPrincipal;
import org.actioncontroller.json.JsonBody;

import java.util.Optional;


public class LogInnController {

    @GET("/NavUser")
    @JsonBody
    public NavUserDto logInnNavUser(@UserPrincipal Optional<PortalRestPrincipal> portalRestPrincipal){
        return portalRestPrincipal
                .map(loggedInUser -> new NavUserDto()
                        .name(loggedInUser.getName())
                        .navIdent(loggedInUser.getNavIdent())
                )
                .orElse(new NavUserDto().name("Name").navIdent("1337"));
    }
}
