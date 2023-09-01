package com.ringo.service.payment;

import com.ringo.model.company.Organisation;

public interface PaymentService {

    String initPayment(Organisation organisation, PaymentData paymentData);

    String createAccount(Organisation organisation);

    String getAccountLink(String accountId);
}
