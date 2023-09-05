package com.ringo.controller;

import com.google.gson.JsonSyntaxException;
import com.ringo.config.ApplicationProperties;
import com.ringo.exception.UserException;
import com.ringo.model.payment.JoiningIntent;
import com.ringo.model.payment.JoiningIntentStatus;
import com.ringo.service.company.JoiningIntentService;
import com.ringo.service.company.TicketService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
@Slf4j
@RequiredArgsConstructor
public class StripeController {
    private final TicketService ticketService;
    private final JoiningIntentService joiningIntentService;
    private final ApplicationProperties config;

    private final String PAYMENT_SUCCESSFUL = "payment_intent.succeeded";
    private final String PAYMENT_FAILED = "payment_intent.payment_failed";

    @PostMapping("/webhook")
    public void webhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String payload) {
        log.info("Webhook from stripe received: {}", payload);

        //validate webhook
        String sigHeader = headers.get("Stripe-Signature");
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, config.getStripeWebhookSecret());
        } catch (JsonSyntaxException e) {
            throw new UserException("Invalid payload");
        } catch (SignatureVerificationException e) {
            throw new UserException("Invalid signature");
        }

        if (event.getType().equals(PAYMENT_SUCCESSFUL)) {
            log.info("Payment successful");
            PaymentIntent paymentIntent = getPaymentIntent(event);
            JoiningIntent joiningIntent = joiningIntentService.changeStatus(paymentIntent.getId(), JoiningIntentStatus.PAYMENT_SUCCEEDED);
            ticketService.issueTicket(joiningIntent);
            //sse notification
        } else if(event.getType().equals(PAYMENT_FAILED)) {
            log.info("Payment failed");
            PaymentIntent paymentIntent = getPaymentIntent(event);
            joiningIntentService.changeStatus(paymentIntent.getId(), JoiningIntentStatus.PAYMENT_FAILED);
            //sse notification
        }
    }

    private PaymentIntent getPaymentIntent(Event event) {
        Optional<StripeObject> object = event.getDataObjectDeserializer().getObject();
        if(object.isPresent()) {
            return (PaymentIntent) object.get();
        } else {
            throw new UserException("Invalid payload");
        }
    }
}
