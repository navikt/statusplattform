package no.nav.portal.infrastructure;


import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.fluentjdbc.util.ExceptionUtil;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.URLEncoder.encode;

public class OpenIdConnectAuthentication implements Authentication.Deferred {

    private static final Logger logger = LoggerFactory.getLogger(OpenIdConnectAuthentication.class);
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String AUTHORIZATION_STATE_COOKIE = "authorization_state";

    private CachedHashMap<String, Principal> cache = new CachedHashMap<>(Duration.ofMinutes(5));

    private static URL openIdConfiguration;
    private static String clientId = System.getenv("AZURE_APP_CLIENT_ID");
    private static String clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET");
    static {
        try{
            System.out.println("clientId: " +clientId);
            openIdConfiguration = new URL(System.getenv("AZURE_APP_WELL_KNOWN_URL"));
            System.out.println("OpenIdConfig: "+ openIdConfiguration);

        }
        catch (MalformedURLException e){
            System.out.println(e);

        }
    }



    @Override
    public Authentication authenticate(ServletRequest servletRequest) {
        return getCookie(servletRequest, ACCESS_TOKEN_COOKIE)
                .flatMap(this::getUser)
                .orElse(this);
    }

    @Override
    public Authentication authenticate(ServletRequest servletRequest, ServletResponse servletResponse) {
        try {
            return doAuthenticate((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Authentication doAuthenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Request uri = " + request.getRequestURI());
        if (request.getRequestURI().startsWith(request.getContextPath() + "/oauth2/callback")) {
            return oauth2callback(request, response);
        } else if (request.getRequestURI().startsWith(request.getContextPath() + "/oauth2")) {
            return redirectToAuthorize(request, response);
        }
        return null;
    }

    @Override
    public Authentication login(String s, Object o, ServletRequest servletRequest) {
        return null;
    }

    @Override
    public Authentication logout(ServletRequest servletRequest) {
        return null;
    }

    private Optional<Authentication> getUser(String accessToken) {
        Principal principal = cache.getOrCompute(accessToken, () -> getPrincipal(accessToken));
        if (principal == null) {
            return Optional.empty();
        }
        MDC.put("remoteUser", principal.getName());
        return Optional.of(new UserAuthentication("brukergrupper-identity", createUserIdentity(principal)));
    }

    protected Optional<Principal> getPrincipal(String accessToken) {
        try {
            OpenIdConfiguration configuration = OpenIdConfiguration.read(openIdConfiguration);
            HttpURLConnection userRequest = configuration.openUserinfoConnection();
            logger.debug("Fetching userinfo");
            userRequest.setRequestProperty("Authorization", "Bearer " + accessToken);
            return Optional.of(createPrincipal(JsonObject.read(userRequest)));
        } catch (JsonHttpException e) {
            logger.warn("Failed to fetch userinfo: {}: {}", e, e.getJsonError());
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Principal createPrincipal(JsonObject userinfo){
        System.out.println(userinfo);
        return new PortalRestPrincipal(userinfo.requiredString("name"), userinfo.requiredString("NAVident"));
    }

    private DefaultUserIdentity createUserIdentity(Principal principal) {
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return new DefaultUserIdentity(subject, principal, new String[0]);
    }

    protected Authentication redirectToAuthorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.addCookie(removeCookie(request, ACCESS_TOKEN_COOKIE));
        String authorizationState = UUID.randomUUID().toString();
        response.addCookie(createCookie(request, AUTHORIZATION_STATE_COOKIE, authorizationState));
        response.sendRedirect(getAuthorizationUrl(request, authorizationState));
        return Authentication.SEND_CONTINUE;
    }

    protected Authentication oauth2callback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean secure = request.isSecure();
        if (!secure && !request.getServerName().equals("localhost")) {
            response.sendError(400, "Must use https");
            return Authentication.SEND_FAILURE;
        }
        if (!isMatchingState(request)) {
            response.sendError(400, "Invalid state");
            return Authentication.SEND_FAILURE;
        }
        response.addCookie(removeCookie(request, AUTHORIZATION_STATE_COOKIE));

        OpenIdConfiguration configuration = OpenIdConfiguration.read(openIdConfiguration);
        HttpURLConnection tokenRequest = configuration.openTokenConnection();
        tokenRequest.setRequestMethod("POST");
        tokenRequest.setDoOutput(true);
        tokenRequest.getOutputStream().write(getTokenPayload(
                getValidatedCode(request),
                getRedirectUri(getContextPath(request))
        ).getBytes());

        int responseCode = tokenRequest.getResponseCode();
        if(responseCode >= 400){
            throw new RuntimeException(String.format("OIDC Authentication failed with code %s and error message %s",  responseCode, stringify(tokenRequest.getErrorStream())));
        }

        JsonObject tokenResponse = JsonObject.read(tokenRequest);
        System.out.println(tokenResponse);
        response.addCookie(createCookie(request, ACCESS_TOKEN_COOKIE, tokenResponse.requiredString("access_token")));
        response.sendRedirect(request.getContextPath());
        return Authentication.SEND_CONTINUE;
    }
    //Flytte denne?
    private static String stringify(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
            return result.toString();
        }
    }

    private String getValidatedCode(HttpServletRequest request) {
        String code = request.getParameter("code");
        if(code == null){
            throw new IllegalArgumentException("Mangler code param");
        }

        String regex = "^[a-zA-Z0-9_-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(code);
        if(!matcher.matches()){
            throw new IllegalArgumentException("Forventer at code samsvarer med gitt pattern. Koden var: " + code);
        }
        return code;
    }

    protected String getTokenPayload(String code, String redirectUri) {
        return "client_id=" + clientId + "&client_secret=" + clientSecret + "&redirect_uri=" + redirectUri + "&code=" + code + "&grant_type=authorization_code";
    }

    protected Cookie createCookie(HttpServletRequest request, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(60 * 30); //IE11 trenger visst verdi her
        cookie.setPath(request.getContextPath());
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        return cookie;
    }

    protected Cookie removeCookie(HttpServletRequest request, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        return cookie;
    }

    private boolean isMatchingState(HttpServletRequest request) {
        String stateCookie = getCookie(request, AUTHORIZATION_STATE_COOKIE).orElse("missing");
        String stateParameter = request.getParameter("state");
        if (!stateCookie.equals(stateParameter)) {
            logger.warn("Unexpected state={} (expected={})", stateParameter, stateCookie);
            return false;
        }
        return true;
    }

    protected String getAuthorizationUrl(HttpServletRequest request, String authorizationState) throws IOException {
        OpenIdConfiguration configuration = OpenIdConfiguration.read(openIdConfiguration);
        return configuration.getAuthorizationEndpoint() + "?" + getAuthorizationQuery(authorizationState, getContextPath(request));
    }

    protected String getContextPath(HttpServletRequest request) {
        return getServerURL(request) + request.getContextPath();
    }

    protected String getAuthorizationQuery(String authorizationState, String contextPath) {
        try {
            return "client_id=" + clientId
                    + "&state=" + authorizationState
                    + "&response_type=code"
                    + "&scope=" + encode("openid profile", Charset.forName("UTF-8").name())
                    + "&redirect_uri=" + getRedirectUri(contextPath);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtil.softenCheckedException(e);
        }
    }

    protected String getRedirectUri(String contextPath) {
        return contextPath + "/login/oauth2callback";
    }




    private Optional<String> getCookie(ServletRequest servletRequest, String name) {
        return Arrays.stream(Optional.ofNullable(((HttpServletRequest) servletRequest).getCookies())
                .orElse(new Cookie[0]))
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findAny();
    }

    public String getServerURL(HttpServletRequest req) {
        String host = Optional.ofNullable(req.getHeader("X-Forwarded-Host"))
                .orElseGet(() -> req.getServerName() + (getServerPort(req) != getDefaultPort(req) ?  ":" + getServerPort(req) : ""));
        return getScheme(req) + "://" + host;
    }

    private int getServerPort(HttpServletRequest req) {
        return Optional.ofNullable(req.getHeader("X-Forwarded-Port"))
                .map(Integer::parseInt)
                .orElseGet(() -> {
                    int port = req.getServerPort();
                    return port == 80 || port == 443 ? getDefaultPort(req) : port;
                });
    }

    private String getScheme(HttpServletRequest req) {
        return Optional.ofNullable(req.getHeader("X-Forwarded-Proto")).orElse(req.getScheme());
    }

    private int getDefaultPort(HttpServletRequest req) {
        return getScheme(req).equals("https") ? 443 : (getScheme(req).equals("http") ? 80 : -1);
    }

}