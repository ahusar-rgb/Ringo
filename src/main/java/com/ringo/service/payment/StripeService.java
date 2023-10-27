package com.ringo.service.payment;

import com.ringo.config.ApplicationProperties;
import com.ringo.exception.InternalException;
import com.ringo.model.company.Organisation;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeService implements PaymentService {

    private final ApplicationProperties config;

    public StripeService(ApplicationProperties config) {
        this.config = config;
        Stripe.apiKey = config.getStripeSecretKey();
    }

    @Override
    public PaymentIntent initPayment(PaymentData paymentData) {
        log.info("Init payment: {}", paymentData);
        Long amount = (long)(paymentData.getAmount() * 100);
        String currency = paymentData.getCurrency().getName().toLowerCase();
        Long applicationFee = (long)(amount * config.getApplicationFeeInPercent() / 100);

        PaymentIntentCreateParams params = getPaymentIntentParams(amount, currency, applicationFee);
        RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(paymentData.getAccountId())
                .setIdempotencyKey(paymentData.getIdempotencyKey())
                .build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params, requestOptions);
            log.info("Payment intent created: {}", paymentIntent.getId());
            return paymentIntent;
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new InternalException("Failed to create payment intent");
        }
    }

    @Override
    public void cancelPayment(String paymentIntentId) {
        log.info("Cancel payment: {}", paymentIntentId);
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            paymentIntent.cancel();
            log.info("Payment intent canceled: {}", paymentIntentId);
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new InternalException("Failed to cancel payment intent");
        }
    }

    @Override
    public String createAccount(Organisation organisation) {
        log.info("Create payment account for user: {}", organisation.getEmail());
        try {
            Account account = Account.create(getAccountDetails(organisation));
            log.info("Account created: {}", account.getId());
            return account.getId();
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new InternalException("Failed to create payment account for user " + organisation.getEmail());
        }
    }

    @Override
    public String getAccountLink(String accountId) {
        AccountLinkCreateParams params =
                AccountLinkCreateParams.builder()
                        .setAccount(accountId)
                        .setRefreshUrl(config.getStripeReauthUrl())
                        .setReturnUrl(config.getStripeReturnUrl())
                        .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                        .build();

        try {
            return AccountLink.create(params).getUrl();
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new InternalException("Failed to create account link");
        }
    }

    private AccountCreateParams getAccountDetails(Organisation organisation) {
        return AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.STANDARD)
                .setEmail(organisation.getEmail())
                .setSettings(
                        AccountCreateParams.Settings.builder()
                                .setPayments(
                                        AccountCreateParams.Settings.Payments.builder()
                                                .setStatementDescriptor("RINGO EVENTS")
                                                .build()
                                ).build()
                ).build();
    }

    private PaymentIntentCreateParams getPaymentIntentParams(Long amount, String currency, Long applicationFee) {
        return PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .setApplicationFeeAmount(applicationFee)
                .build();
    }
}
