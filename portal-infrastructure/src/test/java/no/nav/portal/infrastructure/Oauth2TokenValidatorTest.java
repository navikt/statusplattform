package no.nav.portal.infrastructure;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jose.shaded.json.JSONArray;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class Oauth2TokenValidatorTest {

    void validateToken() throws MalformedURLException, BadJOSEException, ParseException, JOSEException {
        String accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjJaUXBKM1VwYmpBWVhZR2FYRUpsOGxWMFRPSSJ9.eyJhdWQiOiI2YTdlNjhiMC04ODZiLTRiZDItOGU5YS0yYTU4ZTU5YzE2MzIiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vOTY2YWM1NzItZjViNy00YmJlLWFhODgtYzc2NDE5YzBmODUxL3YyLjAiLCJpYXQiOjE2NjkzNjg4MDEsIm5iZiI6MTY2OTM2ODgwMSwiZXhwIjoxNjY5MzcyNzAxLCJhaW8iOiJFMlpnWUdEOUxHSlFIR29abjF1L1ZvMUxnUzhHQUE9PSIsImF6cCI6ImQ4Zjg2ZWY5LTE3MWQtNGRiZC04MjA5LWRhODg4YWU0NTcyNCIsImF6cGFjciI6IjEiLCJvaWQiOiIwMzU4YTMyNC0zYzM3LTQyMTgtOTgzNy1kYmQzMDc0MzIyOWIiLCJyaCI6IjAuQVVjQWNzVnFscmYxdmt1cWlNZGtHY0Q0VWJCb2ZtcHJpTkpManBvcVdPV2NGakpIQUFBLiIsInJvbGVzIjpbImFjY2Vzc19hc19hcHBsaWNhdGlvbiJdLCJzdWIiOiIwMzU4YTMyNC0zYzM3LTQyMTgtOTgzNy1kYmQzMDc0MzIyOWIiLCJ0aWQiOiI5NjZhYzU3Mi1mNWI3LTRiYmUtYWE4OC1jNzY0MTljMGY4NTEiLCJ1dGkiOiJ2M2xKTnhRNnAwLU1lM3YyQ29pX0FBIiwidmVyIjoiMi4wIn0.Ixx_rJCxNKYhA5yZ24cfDZVxwxoKCLQEiQiLWFpzlFhgMPU7RAoW01WxHeTOJ4mFUbVlEcQgdQNwYEAZ77H0wkTD4eAxznO4ttlKa3l1Q4vxkve0KcjGztEhj0ncaB0rRkqzK9BuQ62LFD6KroXa7u1NMdRk7f9YWAFYIpPZMGZ3qPakpD0wNOj9MkTZNl8euaxSvOW6f2LyGnd5lYnFBFkyrsWCTV8qo4frtPS6TZLfPhP6P6nlTxHRcOi0qhMMHibViGWxoxUPNRXjnNiP2VGpStfuJ8obteSE1tB17dD55nHsKygF2k0Zz2nkC-FsQKpyJlUphXOYo6iG2FRhjg";

        Oauth2TokenValidator validator = new Oauth2TokenValidator();
        JWTClaimsSet claimsSet = validator.doValidateAndGetClaims(accessToken);
        JSONArray roles = (JSONArray)claimsSet.getClaim("roles");
        boolean isApplication = roles.size() == 1 && roles.get(0).equals("access_as_application");
        String hello = "hello";
    }


}