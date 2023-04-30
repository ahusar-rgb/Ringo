package com.ringo.dto.company;

import com.ringo.dto.common.Coordinates;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventGroupDto {
    private Coordinates coordinates;
    private Integer count;
    private Long mainPhotoId;
    private Long id;
}
