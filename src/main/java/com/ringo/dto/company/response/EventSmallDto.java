package com.ringo.dto.company.response;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.CategoryDto;
import com.ringo.dto.company.CurrencyDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class EventSmallDto extends AbstractEntityDto {
    private String name;
    private String description;
    private Boolean isActive;
    private String address;
    private Coordinates coordinates;
    private Long mainPhotoId;
    private Integer distance;
    private Boolean isTicketNeeded;
    private Float price;
    private CurrencyDto currency;
    private String startTime;
    private String endTime;
    private List<CategoryDto> categories;
    private Long hostId;
    private Integer peopleCount;
    private Integer capacity;
}
