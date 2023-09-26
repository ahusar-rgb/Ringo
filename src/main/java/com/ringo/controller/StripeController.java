package com.ringo.controller;

import com.ringo.service.payment.StripeWebhookHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@Slf4j
@RequiredArgsConstructor
public class StripeController {
    private final StripeWebhookHandler webhookHandler;

    @PostMapping("/webhook")
    public void webhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String payload) {
        webhookHandler.processWebhook(headers, payload);
    }
}
