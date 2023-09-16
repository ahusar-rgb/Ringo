package com.ringo.dto.company.response;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.dto.common.Coordinates;
import com.ringo.dto.company.CategoryDto;
import com.ringo.dto.company.CurrencyDto;
import com.ringo.dto.photo.EventMainPhotoDto;
import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.model.form.RegistrationForm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class EventResponseDto extends AbstractEntityDto {
    private String name;
    private String description;
    private Boolean isActive;
    private EventMainPhotoDto mainPhoto;
    private List<EventPhotoDto> photos;
    private String address;
    private Coordinates coordinates;
    private Boolean isTicketNeeded;
    private List<TicketTypeResponseDto> ticketTypes;
    private String startTime;
    private String endTime;
    private List<CategoryDto> categories;
    private OrganisationResponseDto host;
    private Float price;
    private CurrencyDto currency;
    private Integer capacity;
    private Integer peopleCount;
    private Boolean isSaved;
    private Integer peopleSaved;
    private Boolean isRegistered;
    private RegistrationForm registrationForm;
}
