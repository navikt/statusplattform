package no.nav.portal.infrastructure;


import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.fluentjdbc.util.ExceptionUtil;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.security.token.support.core.validation.DefaultJwtTokenValidator;

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
import java.util.*;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.nav.security.token.support.core.validation.ConfigurableJwtTokenValidator;

import static java.net.URLEncoder.encode;

public class OpenIdConnectAuthentication implements Authentication.Deferred {

    private static final Logger logger = LoggerFactory.getLogger(OpenIdConnectAuthentication.class);
    public static final String ID_TOKEN_COOKIE = "tmp_token";
    public static final String AUTHORIZATION_STATE_COOKIE = "authorization_state";
    public static final String AUTHENTICATION_HEADER= "Authenticate";

    private CachedHashMap<String, Principal> cache = new CachedHashMap<>(Duration.ofMinutes(1));


    private static String CLIENT_ID = System.getenv("AZURE_APP_CLIENT_ID");
    private static String CLIENT_SECRET = System.getenv("AZURE_APP_CLIENT_SECRET");
    private static String PUBLIC_JWKS = System.getenv("AZURE_OPENID_CONFIG_JWKS_URI");
    private static String AZURE_OPENID_CONFIG_ISSUER = System.getenv("AZURE_OPENID_CONFIG_ISSUER");
    private static URL AZURE_WELL_KNOW_URL;
    private static String FRONTEND_LOCATION;

    private int COOKIE_SESSION_TIMEOUT_DURATION_IN_WEEKS = 60*60*24*7;

    static {
        try{
            AZURE_WELL_KNOW_URL = new URL(System.getenv("AZURE_APP_WELL_KNOWN_URL"));
            FRONTEND_LOCATION = System.getenv("FRONTEND_LOCATION");

        }
        catch (MalformedURLException e){
            logger.info(e.toString());

        }
    }

    protected Authentication oauth2callback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("oauth2callback ---------------------------");

        String authorization = request.getHeader("Authorization");
        Enumeration<String> headers = request.getHeaderNames();
        logger.info("autorization: " + authorization);
        logger.info("autorization decoded: " + decodeBase64Url(authorization.getBytes(StandardCharsets.UTF_8)) );
        while(headers.hasMoreElements()){
            logger.info("Header element: "+   headers.nextElement());
        }



        //DefaultJwtTokenValidator tokenValidator = new DefaultJwtTokenValidator(AZURE_OPENID_CONFIG_ISSUER,List.of(CLIENT_ID),new RemoteJWKSet(AZURE_WELL_KNOW_URL));

        response.setStatus(200);
        response.sendRedirect(FRONTEND_LOCATION);



        return Authentication.SEND_CONTINUE;
    }



    @Override
    public Authentication authenticate(ServletRequest servletRequest) {
        System.out.println("authenticate(ServletRequest servletRequest) ---------------------------");
        //Sette verdier fra header isteden for cookie
        return getUserv2(servletRequest)
                .orElse(this);
    }

    private Optional<Authentication> getUserv2(ServletRequest servletRequest) {
        logger.info("getUser ---------------------------");

        String encodedAuthentication = ((HttpServletRequest) servletRequest).getHeader(AUTHENTICATION_HEADER);
        if(encodedAuthentication.isEmpty()){
            return Optional.empty();
        }
        String[] splited = encodedAuthentication.split("[.]");

        String encodedHeader = splited[0];
        String encodedPayload = splited[1];
        String encodedSignature = splited[2];

        String decodedHeader  = new String(Base64.getDecoder().decode(encodedHeader));
        String decodedPayload  = new String(Base64.getDecoder().decode(encodedPayload));

        JsonObject headerJson =  JsonObject.parse(decodedHeader);
        JsonObject payloadJson = JsonObject.parse(decodedPayload);
        PortalRestPrincipal principal = createPrincipalv2(payloadJson);

        logger.info("Useridentity: "+ principal.getName() + principal.getNavIdent());
        return Optional.of(new UserAuthentication("user", createUserIdentity(principal)));
    }

    public PortalRestPrincipal createPrincipalv2(JsonObject payloadJson){
        System.out.println("createPrincipal ---------------------------");
        logger.info(payloadJson.toJson());
        return new PortalRestPrincipal(payloadJson.requiredString("name"), payloadJson.stringValue("NAVident").orElse(null));
    }

    @Override
    public Authentication authenticate(ServletRequest servletRequest, ServletResponse servletResponse) {

        System.out.println("authenticate ---------------------------");
        try {
            return doAuthenticate((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Authentication doAuthenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Request uri = " + request.getRequestURI());
        if (request.getRequestURI().startsWith(request.getContextPath() + "/callback")) {
            return oauth2callback(request, response);
        } else if (request.getRequestURI().startsWith(request.getContextPath() + "/login")) {
            return redirectToAuthorize(request, response);
        }
        else if (request.getRequestURI().startsWith(request.getContextPath() + "/logout")) {
            return logout(request);
        }
        return null;
    }

    @Override
    public Authentication login(String s, Object o, ServletRequest servletRequest) {
        return null;
    }


    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.addCookie(removeCookie(request, ID_TOKEN_COOKIE));
            request.getSession().invalidate();
            logOutAzure(response);

        }catch (Exception e){

        }
    }




    private Optional<Authentication> getUser(String idToken) {
        System.out.println("getUser ---------------------------");
        Principal principal = cache.getOrCompute(idToken, () -> getPrincipal(idToken));
        if (principal == null) {
            return Optional.empty();
        }
        MDC.put("remoteUser", principal.getName());
        return Optional.of(new UserAuthentication("brukergrupper-identity", createUserIdentity(principal)));
    }


    protected Optional<Principal> getPrincipal(String idToken) {
        System.out.println("getPrincipal ---------------------------");
        try {
            /*OpenIdConfiguration configuration = OpenIdConfiguration.read(openIdConfiguration);
            HttpURLConnection userRequest = configuration.openUserinfoConnection();
            logger.debug("Fetching userinfo");
            userRequest.setRequestProperty("Authorization", "Bearer " + idToken);
            return Optional.of(createPrincipal(JsonObject.read(userRequest)));*/

            String jsonIdToken = new String(Base64.getDecoder().decode(idToken.split("\\.")[1]), StandardCharsets.UTF_8);
            return Optional.of(createPrincipal(JsonObject.parse(jsonIdToken)));
        } catch (JsonHttpException e) {
            logger.warn("Failed to fetch userinfo: {}: {}", e, e.getJsonError());
            return Optional.empty();
        }
    }

    public Principal createPrincipal(JsonObject userinfo){
        System.out.println("createPrincipal ---------------------------");
        logger.info(userinfo.toJson());
        return new PortalRestPrincipal(userinfo.requiredString("name"), userinfo.stringValue("NAVident").orElse(null));
    }

    private DefaultUserIdentity createUserIdentity(Principal principal) {
        System.out.println("createUserIdentity ---------------------------");
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return new DefaultUserIdentity(subject, principal, new String[0]);
    }

    protected Authentication redirectToAuthorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("redirectToAuthorize ---------------------------");
/*
        response.sendRedirect(getAuthorizationUrl(request, authorizationState));

        String authorizationState = UUID.randomUUID().toString();
        response.addCookie(removeCookie(request, ID_TOKEN_COOKIE));
        response.addCookie(createCookie(request, AUTHORIZATION_STATE_COOKIE, authorizationState));

z           */



        response.sendRedirect("https://digitalstatus.ekstern.dev.nav.no" +"/oauth2/login?redirect="+ "/authenticate/callback");

        return Authentication.SEND_CONTINUE;
    }

    private String decodeBase64Url(byte[] encoded){
        return Base64.getUrlEncoder().encodeToString(encoded);
    }



    protected Authentication oauth2callbackOld(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("oauth2callback ---------------------------");
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

        OpenIdConfiguration configuration = OpenIdConfiguration.read(AZURE_WELL_KNOW_URL);
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

        String id_token = tokenResponse.requiredString("id_token");
        response.addCookie(createCookie(request, ID_TOKEN_COOKIE, id_token));
        response.sendRedirect("frontEndUrl" + "/Dashboard/Privatperson/");
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

    private void logOutAzure(HttpServletResponse response) throws IOException {
        OpenIdConfiguration configuration = OpenIdConfiguration.read(AZURE_WELL_KNOW_URL);
        System.out.println("Endsession!:" +configuration.getEndSessionEndpoint().toString());

        response.sendRedirect(configuration.getEndSessionEndpoint() + "?post_logout_redirect_uri=" + (FRONTEND_LOCATION + "/Dashboard/Privatperson/"));

    }

    private String getValidatedCode(HttpServletRequest request) {
        String code = request.getParameter("code");
        if(code == null){
            throw new IllegalArgumentException("Mangler code param");
        }

        String regex = "^[a-zA-Z0-9_\\-.]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(code);
        if(!matcher.matches()){
            throw new IllegalArgumentException("Forventer at code samsvarer med gitt pattern. Koden var: " + code);
        }
        return code;
    }

    protected String getTokenPayload(String code, String redirectUri) {
        return "client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&redirect_uri=" + redirectUri + "&code=" + code + "&grant_type=authorization_code";
    }

    protected Cookie createCookie(HttpServletRequest request, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(COOKIE_SESSION_TIMEOUT_DURATION_IN_WEEKS); //IE11 trenger visst verdi her
        cookie.setPath(request.getContextPath());
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        return cookie;
    }

    protected Cookie removeCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
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
        if (!stateCookie.equals(stateParameter) && !getServerURL(request).equals("http://localhost:3000")) {
            logger.warn("Unexpected state={} (expected={})", stateParameter, stateCookie);
            return false;
        }
        return true;
    }

    protected String getAuthorizationUrl(HttpServletRequest request, String authorizationState) throws IOException {
        OpenIdConfiguration configuration = OpenIdConfiguration.read(AZURE_WELL_KNOW_URL);
        return configuration.getAuthorizationEndpoint() + "?" + getAuthorizationQuery(authorizationState, getContextPath(request));
    }

    protected String getContextPath(HttpServletRequest request) {
        return getServerURL(request) + request.getContextPath();
    }

    protected String getAuthorizationQuery(String authorizationState, String contextPath) {
        try {
            return "client_id=" + CLIENT_ID
                    + "&state=" + authorizationState
                    + "&response_type=code"
                    + "&scope=" + encode("openid profile", Charset.forName("UTF-8").name())
                    + "&redirect_uri=" + getRedirectUri(contextPath);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtil.softenCheckedException(e);
        }
    }

    protected String getRedirectUri(String contextPath) {
        return  FRONTEND_LOCATION + "/oauth2/callback";
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

    @Override
    public Authentication logout(ServletRequest request) {
        return null;
    }
}
