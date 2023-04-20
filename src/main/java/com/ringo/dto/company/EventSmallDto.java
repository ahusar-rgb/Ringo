package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.dto.common.Coordinates;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class EventSmallDto extends AbstractEntityDto {
    private String name;
    private String description;
    private String address;
    private Coordinates coordinates;
    private Integer distance;
    private Boolean isTicketNeeded;
    private Float price;
    private CurrencyDto currency;
    private String startTime;
    private String endTime;
    private List<CategoryDto> categories;
    private byte[] hostPhoto;
    private Long hostId;
    private Integer peopleCount;
    private Integer capacity;
}
