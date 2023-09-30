package com.ringo.dto.company;

import com.ringo.dto.company.response.TicketDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinEventResult {
    private String paymentIntentClientSecret;
    private TicketDto ticket;
}
