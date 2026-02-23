
package no.nav.statusplattform.api.Helpers;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.models.*;
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final GraphServiceClient graphClient;
    private final String mailbox;
    private final String statusPageUrl;

    public EmailService() {
        String clientId = System.getenv("AZURE_APP_CLIENT_ID");
        String clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET");
        String tenantId = System.getenv("AZURE_APP_TENANT_ID");

        this.mailbox = System.getenv().getOrDefault("NOTIFICATION_MAILBOX", "statusplattform@nav.no");
        this.statusPageUrl = System.getenv().getOrDefault("STATUS_PAGE_URL", "https://status.nav.no");

        var credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        this.graphClient = new GraphServiceClient(credential, "https://graph.microsoft.com/.default");
    }

    public void sendOtpEmail(String email, String otpCode) {
        String html = loadTemplate("email-templates/otp-email.html")
                .replace("{{OTP_CODE}}", otpCode)
                .replace("{{STATUS_PAGE_URL}}", statusPageUrl);

        sendEmail(email, "Bekreft e-postadressen din - NAV Statusplattform", html);
    }

    public void sendNotificationEmail(String email, String subject, String bodyHtml) {
        String html = loadTemplate("email-templates/notification-ops-message.html")
                .replace("{{SUBJECT}}", subject)
                .replace("{{BODY}}", bodyHtml)
                .replace("{{UNSUBSCRIBE_URL}}", statusPageUrl + "/avmeld?email=" + email)
                .replace("{{STATUS_PAGE_URL}}", statusPageUrl);

        sendEmail(email, subject, html);
    }

    public void sendUnsubscribeConfirmation(String email) {
        String html = loadTemplate("email-templates/notification-ops-message.html")
                .replace("{{SUBJECT}}", "Avmelding bekreftet")
                .replace("{{BODY}}", "Du er nå avmeldt statusoppdateringer fra NAV. Du vil ikke lenger motta varsler om driftsmeldinger eller statusendringer.")
                .replace("{{UNSUBSCRIBE_URL}}", "")
                .replace("{{STATUS_PAGE_URL}}", statusPageUrl);

        sendEmail(email, "Avmelding bekreftet - NAV Statusplattform", html);
    }

    private void sendEmail(String toEmail, String subject, String bodyHtml) {
        try {
            var message = new Message();
            message.setSubject(subject);

            var body = new ItemBody();
            body.setContentType(BodyType.Html);
            body.setContent(bodyHtml);
            message.setBody(body);

            var recipient = new Recipient();
            var emailAddress = new EmailAddress();
            emailAddress.setAddress(toEmail);
            recipient.setEmailAddress(emailAddress);
            message.setToRecipients(List.of(recipient));

            var sendMailPostRequestBody = new SendMailPostRequestBody();
            sendMailPostRequestBody.setMessage(message);
            sendMailPostRequestBody.setSaveToSentItems(false);

            graphClient.users().byUserId(mailbox).sendMail().post(sendMailPostRequestBody);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String loadTemplate(String templatePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (is == null) {
                logger.error("Email template not found: {}", templatePath);
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to load email template {}: {}", templatePath, e.getMessage(), e);
            return "";
        }
    }
}
