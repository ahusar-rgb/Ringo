package com.ringo.service.payment;

import com.ringo.model.company.Organisation;
import com.stripe.model.PaymentIntent;

public interface PaymentService {

    PaymentIntent initPayment(PaymentData paymentData);

    String createAccount(Organisation organisation);

    String getAccountLink(String accountId);
}
