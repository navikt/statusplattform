package no.nav.statusplattform.api.Helpers;

import nav.statusplattform.core.entities.OtpVerificationEntity;
import nav.statusplattform.core.entities.SubscriptionEntity;
import nav.statusplattform.core.repositories.OtpRepository;
import nav.statusplattform.core.repositories.SubscriptionRepository;
import org.actioncontroller.HttpRequestException;
import org.fluentjdbc.DbContext;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubscriptionControllerHelper {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionControllerHelper.class);

    private final SubscriptionRepository subscriptionRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public SubscriptionControllerHelper(DbContext dbContext, EmailService emailService) {
        this.subscriptionRepository = new SubscriptionRepository(dbContext);
        this.otpRepository = new OtpRepository(dbContext);
        this.emailService = emailService;
    }

    public JsonObject sendOtp(String email) {
        int rateLimitCount = otpRepository.getRateLimitCount(email);
        if (rateLimitCount >= 5) {
            throw new HttpRequestException("Rate limit exceeded");
        }

        String otpCode = String.format("%06d", new SecureRandom().nextInt(999999));

        OtpVerificationEntity otp = new OtpVerificationEntity()
                .setEmail(email)
                .setOtpCode(otpCode)
                .setExpiresAt(ZonedDateTime.now().plusMinutes(10))
                .setVerified(false)
                .setAttempts(0);

        otpRepository.save(otp);
        otpRepository.incrementRateLimit(email);
        emailService.sendOtpEmail(email, otpCode);

        logger.info("OTP sent to {}", email);
        return new JsonObject().put("message", "OTP sendt");
    }

    public JsonObject verifyOtp(String email, String code) {
        Optional<OtpVerificationEntity> otpOpt = otpRepository.findValidOtp(email, code);

        if (otpOpt.isEmpty()) {
            throw new HttpRequestException("Ugyldig eller utløpt kode");
        }

        OtpVerificationEntity otp = otpOpt.get();

        if (otp.getAttempts() >= 3) {
            throw new HttpRequestException("For mange forsøk");
        }

        otpRepository.markVerified(otp.getId());
        subscriptionRepository.markEmailVerified(email);

        logger.info("OTP verified for {}", email);
        return new JsonObject().put("verified", true);
    }

    public JsonObject createSubscription(String email, List<UUID> serviceIds, boolean isInternal) {
        Optional<SubscriptionEntity> existingOpt = subscriptionRepository.findByEmail(email);

        SubscriptionEntity subscription;
        if (existingOpt.isPresent()) {
            subscription = existingOpt.get();
            subscriptionRepository.setServicesOnSubscription(subscription.getId(), serviceIds);
            logger.info("Updated existing subscription for {}", email);
        } else {
            subscription = new SubscriptionEntity()
                    .setEmail(email)
                    .setEmailVerified(false)
                    .setInternal(isInternal);
            UUID id = subscriptionRepository.save(subscription);
            subscription.setId(id);
            subscription = subscriptionRepository.retrieve(id).orElseThrow();
            subscriptionRepository.setServicesOnSubscription(id, serviceIds);
            logger.info("Created new subscription for {}", email);
        }

        return toSubscriptionJson(subscription, serviceIds);
    }

    public JsonObject getSubscription(UUID token) {
        SubscriptionEntity subscription = subscriptionRepository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new HttpRequestException("Subscription not found"));

        List<UUID> serviceIds = subscriptionRepository.getServiceIdsForSubscription(subscription.getId());
        return toSubscriptionJson(subscription, serviceIds);
    }

    public JsonObject updateSubscription(UUID token, List<UUID> serviceIds) {
        SubscriptionEntity subscription = subscriptionRepository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new HttpRequestException("Subscription not found"));

        subscriptionRepository.setServicesOnSubscription(subscription.getId(), serviceIds);
        logger.info("Updated subscription services for {}", subscription.getEmail());

        return toSubscriptionJson(subscription, serviceIds);
    }

    public JsonObject unsubscribe(UUID token) {
        Optional<SubscriptionEntity> subscriptionOpt = subscriptionRepository.findByUnsubscribeToken(token);

        if (subscriptionOpt.isPresent()) {
            String email = subscriptionOpt.get().getEmail();
            subscriptionRepository.deleteByUnsubscribeToken(token);
            emailService.sendUnsubscribeConfirmation(email);
            logger.info("Unsubscribed {}", email);
        }

        return new JsonObject().put("message", "Avmelding bekreftet");
    }

    private JsonObject toSubscriptionJson(SubscriptionEntity subscription, List<UUID> serviceIds) {
        JsonArray serviceIdArray = new JsonArray();
        serviceIds.forEach(id -> serviceIdArray.add(id.toString()));

        return new JsonObject()
                .put("email", subscription.getEmail())
                .put("serviceIds", serviceIdArray)
                .put("unsubscribeToken", subscription.getUnsubscribeToken().toString())
                .put("isInternal", subscription.isInternal());
    }
}
