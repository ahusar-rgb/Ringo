package com.ringo.dto.company.request;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.dto.common.Coordinates;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class EventRequestDto extends AbstractEntityDto {
    @Pattern(regexp = "^.{3,128}$", message = "Name must be between 3 and 128 characters")
    private String name;
    @Pattern(regexp = "^.{3,5000}$", message = "Description must be between 3 and 5000 characters")
    private String description;
    private String address;
    private Coordinates coordinates;
    private Boolean isTicketNeeded;
    @Size(max = 5, message = "Number of contacts must be between 0 and 5")
    private List<@Valid TicketTypeRequestDto> ticketTypes;
    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$", message = "Start time must be in format yyyy-MM-ddTHH:mm:ss")
    private String startTime;
    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$", message = "End time must be in format yyyy-MM-ddTHH:mm:ss")
    private String endTime;
    private Float price;
    private Long currencyId;
    private Integer capacity;
    private List<Long> categoryIds;
}
