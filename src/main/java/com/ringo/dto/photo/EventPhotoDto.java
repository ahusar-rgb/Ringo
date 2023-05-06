package com.ringo.dto.photo;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class EventPhotoDto {
    private Long id;
    private Long normalId;
    private Long lazyId;
}
