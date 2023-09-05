package com.ringo.dto.company;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinEventResult {
    private String paymentIntentClientSecret;
    private TicketDto ticket;
}
