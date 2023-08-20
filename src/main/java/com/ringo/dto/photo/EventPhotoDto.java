package com.ringo.dto.photo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class EventPhotoDto {
    private Long id;
    private Long normalId;
    private Long lazyId;
    private Integer ordinal;
}
