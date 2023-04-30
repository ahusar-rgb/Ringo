package com.ringo.dto.photo;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class EventMainPhotoDto{
    private Long highQualityId;
    private Long mediumQualityId;
    private Long lowQualityId;
    private Long lazyId;
}
