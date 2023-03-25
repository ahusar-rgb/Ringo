package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
import com.ringo.dto.common.Coordinates;
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
@SuperBuilder
public class EventRequestDto extends AbstractEntityDto {
    private String name;
    private String description;
    private String address;
    private Coordinates coordinates;
    private Boolean isTicketNeeded;
    private Float price;
    private Long currencyId;
    private String startTime;
    private String endTime;
    private List<Long> categoryIds;
    private Long organisationId;
    private Integer photoCount;
}
