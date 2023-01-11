package no.nav.portal.infrastructure;

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

    public String getToken_type() {
        return token_type;
    }

    public OBOtoken setToken_type(String token_type) {
        this.token_type = token_type;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public OBOtoken setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public OBOtoken setExpires_in(String expires_in) {
        this.expires_in = expires_in;
        return this;
    }

    public String getAccess_token() {
        return access_token;
    }

    public OBOtoken setAccess_token(String access_token) {
        this.access_token = access_token;
        return this;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public OBOtoken setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
        return this;
    }
}

