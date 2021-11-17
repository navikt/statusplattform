package no.nav.portal.infrastructure;

import javax.security.auth.Subject;
import java.security.Principal;

public class PortalRestPrincipal implements Principal {
    private String name;
    private String navIdent;

    public PortalRestPrincipal(String name,String navIdent) {
        this.name = name;
        this.navIdent = navIdent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
    public String getNavIdent() {
        return navIdent;
    }
}
