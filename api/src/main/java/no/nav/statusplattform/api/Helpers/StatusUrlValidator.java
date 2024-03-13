package no.nav.statusplattform.api.Helpers;

import org.actioncontroller.HttpRequestException;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusUrlValidator {

    private static final Logger logger = LoggerFactory.getLogger(StatusUrlValidator.class);

    private static final  String STATUSHOLDER = "STATUSHOLDER";
    // TODO: Replace w/AppConfig
    private static final String STATUSHOLDER_URL = System.getenv("statusholder_url");
    private static final String STATUSHOLDER_ENDPOINTCECK = STATUSHOLDER_URL+ "/checkSatusEndpoint/?url=";

    public static Boolean validateUrl(String pollingUrl){
        if(pollingUrl == null || pollingUrl.equals("")){
            return true;
        }
        String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (urlValidator.isValid(pollingUrl) || STATUSHOLDER.equals(pollingUrl)) {
            return true;
        } else {
            throw new HttpRequestException("Polling not valid: "+ pollingUrl);
        }
    }
}
