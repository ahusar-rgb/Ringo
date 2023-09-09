package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.dto.common.Coordinates;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
    @Min(value = 1, message = "Price must be greater than 0")
    private Float price;
    private Long currencyId;
    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$", message = "Start time must be in format yyyy-MM-ddTHH:mm:ss")
    private String startTime;
    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$", message = "End time must be in format yyyy-MM-ddTHH:mm:ss")
    private String endTime;
    private List<Long> categoryIds;
    @Min(value = 1, message = "Capacity must be greater than 0")
    private Integer capacity;
}
