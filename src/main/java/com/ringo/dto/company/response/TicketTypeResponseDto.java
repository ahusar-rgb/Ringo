package com.ringo.dto.company.response;

import com.ringo.dto.company.CurrencyDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketTypeResponseDto {
    private Long id;
    private String title;
    private String description;
    private Float price;
    private CurrencyDto currency;
    private Integer peopleCount;
    private Integer maxTickets;
    private String salesStopTime;
}
