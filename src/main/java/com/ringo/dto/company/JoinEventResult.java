package com.ringo.dto.company;

import com.ringo.dto.company.response.TicketDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinEventResult {
    private String paymentIntentClientSecret;
    private String organisationAccountId;
    private TicketDto ticket;

    public static JoinEventResult fromTicket(TicketDto ticket) {
        return JoinEventResult.builder()
                .ticket(ticket)
                .build();
    }

    public static JoinEventResult fromPaymentIntent(String paymentIntentClientSecret, String organisationAccountId) {
        return JoinEventResult.builder()
                .paymentIntentClientSecret(paymentIntentClientSecret)
                .organisationAccountId(organisationAccountId)
                .build();
    }
}
