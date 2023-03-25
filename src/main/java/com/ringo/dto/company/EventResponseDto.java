package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.dto.common.Coordinates;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class EventResponseDto extends AbstractEntityDto {
    private String name;
    private String description;
    private byte[] mainPhoto;
    private List<byte[]> photos;
    private String address;
    private Coordinates coordinates;
    private Boolean isTicketNeeded;
    private Float price;
    private CurrencyDto currency;
    private String startTime;
    private String endTime;
    private List<CategoryDto> categories;
    private OrganisationResponseDto organisation;
}
