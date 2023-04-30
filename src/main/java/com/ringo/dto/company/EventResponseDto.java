package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.photo.EventMainPhotoDto;
import com.ringo.dto.photo.EventPhotoDto;
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
    private EventMainPhotoDto mainPhoto;
    private List<EventPhotoDto> photos;
    private String address;
    private Coordinates coordinates;
    private Boolean isTicketNeeded;
    private Float price;
    private CurrencyDto currency;
    private String startTime;
    private String endTime;
    private List<CategoryDto> categories;
    private OrganisationResponseDto host;
    private Integer peopleCount;
    private Integer capacity;
}
