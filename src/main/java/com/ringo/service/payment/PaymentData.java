package com.ringo.service.payment;

import com.ringo.model.company.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentData {
    private Float amount;
    private Currency currency;
}
