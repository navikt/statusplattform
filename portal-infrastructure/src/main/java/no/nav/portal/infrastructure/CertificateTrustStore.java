package no.nav.portal.infrastructure;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateTrustStore {

    public static CertificateTrustStore loadFromDirectory(File directory) throws IOException, GeneralSecurityException {
        return new CertificateTrustStore(directory.listFiles(
                f -> f.getName().endsWith(".crt") || f.getName().endsWith(".cer"))
        );
    }

    private final KeyStore trustStore;

    public CertificateTrustStore(File[] files) throws IOException, GeneralSecurityException {
        trustStore = createTrustStore(files);
    }

    public SSLSocketFactory createSslSocketFactory() throws GeneralSecurityException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        TrustManager[] trustManagers = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManagers, null);
        return sslContext.getSocketFactory();
    }

    private static KeyStore createTrustStore(File[] certificateFiles) throws IOException, GeneralSecurityException {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        for (File certificateFile : certificateFiles) {
            trustStore.setCertificateEntry(certificateFile.getName(), loadCertificate(certificateFile));
        }
        addDefaultRootCaCertificates(trustStore);
        return trustStore;
    }


    private static X509Certificate loadCertificate(File certificateFile) throws IOException, CertificateException {
        try (FileInputStream inputStream = new FileInputStream(certificateFile)) {
            return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(inputStream);
        }
    }

    private static void addDefaultRootCaCertificates(KeyStore trustStore) throws GeneralSecurityException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                for (X509Certificate acceptedIssuer : ((X509TrustManager) trustManager).getAcceptedIssuers()) {
                    trustStore.setCertificateEntry(acceptedIssuer.getSubjectDN().getName(), acceptedIssuer);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        CertificateTrustStore certificateTrustStore = CertificateTrustStore.loadFromDirectory(new File("."));

        URL url = new URL("https://login.local.barentswatch.net:10443/");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(certificateTrustStore.createSslSocketFactory());
        System.out.println(conn.getResponseCode());
    }
}