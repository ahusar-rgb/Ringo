package com.ringo.dto.company.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketTypeRequestDto {
    @Min(0)
    @NotNull
    private Integer ordinal;
    @Pattern(regexp = "^.{1,30}$", message = "Ticket title must be between 1 and 30 characters")
    private String title;
    @Pattern(regexp = "^.{1,100}$", message = "Ticket description must be between 1 and 100 characters")
    private String description;
    @NotNull
    @Min(value = 0, message = "Ticket price must be greater than 0")
    private Float price;
    @NotNull
    private Long currencyId;
    @Min(value = 0, message = "Ticket maxTickets must be greater than 0")
    private Integer maxTickets;
    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$", message = "Sales stop time must be in format yyyy-MM-ddTHH:mm:ss")
    private String salesStopTime;
}
