package no.nav.statusplattform.api.v3.controllers;

import no.nav.statusplattform.api.Helpers.EmailService;
import no.nav.statusplattform.api.Helpers.SubscriptionControllerHelper;
import org.actioncontroller.GET;
import org.actioncontroller.POST;
import org.actioncontroller.PUT;
import org.actioncontroller.PathParam;
import org.actioncontroller.json.JsonBody;
import org.fluentjdbc.DbContext;
import org.jsonbuddy.JsonObject;

import java.util.UUID;

public class SubscriptionController {

    private final SubscriptionControllerHelper subscriptionControllerHelper;

    public SubscriptionController(DbContext dbContext, EmailService emailService) {
        this.subscriptionControllerHelper = new SubscriptionControllerHelper(dbContext, emailService);
    }

    @POST("/Subscription/SendOtp")
    @JsonBody
    public JsonObject sendOtp(@JsonBody JsonObject body) {
        String email = body.requiredString("email");
        return subscriptionControllerHelper.sendOtp(email);
    }

    @POST("/Subscription/VerifyOtp")
    @JsonBody
    public JsonObject verifyOtp(@JsonBody JsonObject body) {
        String email = body.requiredString("email");
        String code = body.requiredString("code");
        return subscriptionControllerHelper.verifyOtp(email, code);
    }

    @POST("/Subscription")
    @JsonBody
    public JsonObject createSubscription(@JsonBody JsonObject body) {
        String email = body.requiredString("email");
        java.util.List<UUID> serviceIds = body.requiredArray("serviceIds")
                .strings().stream().map(UUID::fromString).toList();
        boolean isInternal = body.requiredBoolean("isInternal");
        return subscriptionControllerHelper.createSubscription(email, serviceIds, isInternal);
    }

    @GET("/Subscription/Unsubscribe/:token")
    @JsonBody
    public JsonObject unsubscribe(@PathParam("token") UUID token) {
        return subscriptionControllerHelper.unsubscribe(token);
    }

    @GET("/Subscription/:token")
    @JsonBody
    public JsonObject getSubscription(@PathParam("token") UUID token) {
        return subscriptionControllerHelper.getSubscription(token);
    }

    @PUT("/Subscription/:token")
    @JsonBody
    public JsonObject updateSubscription(@PathParam("token") UUID token, @JsonBody JsonObject body) {
        java.util.List<UUID> serviceIds = body.requiredArray("serviceIds")
                .strings().stream().map(UUID::fromString).toList();
        return subscriptionControllerHelper.updateSubscription(token, serviceIds);
    }
}
