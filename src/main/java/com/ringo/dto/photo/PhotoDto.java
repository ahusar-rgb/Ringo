package com.ringo.dto.photo;

import com.ringo.dto.common.AbstractEntityDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class PhotoDto extends AbstractEntityDto {
    private byte[] content;
    private String path;
    private String contentType;
}
