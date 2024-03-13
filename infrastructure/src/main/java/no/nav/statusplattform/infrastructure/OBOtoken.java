package no.nav.statusplattform.infrastructure;

public class OBOtoken {
    private String token_type;
    private String scope;
    private String expires_in;
    private String access_token;
    private String refresh_token;

    public OBOtoken(String token_type, String scope, String expires_in, String access_token, String refresh_token) {
        this.token_type = token_type;
        this.scope = scope;
        this.expires_in = expires_in;
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

    public String getScope() {
        return scope;
    }

    public OBOtoken setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getAccess_token() {
        return access_token;
    }
}
