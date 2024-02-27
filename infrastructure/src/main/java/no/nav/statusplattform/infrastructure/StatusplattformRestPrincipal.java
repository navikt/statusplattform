package no.nav.statusplattform.infrastructure;

import javax.security.auth.Subject;
import java.security.Principal;

public class StatusplattformRestPrincipal implements Principal {
    private String name;
    private String navIdent;

    public StatusplattformRestPrincipal(String name, String navIdent) {
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
