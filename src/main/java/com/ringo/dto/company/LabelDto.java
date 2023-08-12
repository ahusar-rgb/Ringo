package com.ringo.dto.company;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabelDto {
    private Long id;
    private Integer ordinal;
    private String title;
    private String content;
}
