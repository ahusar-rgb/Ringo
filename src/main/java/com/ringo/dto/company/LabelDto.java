package com.ringo.dto.company;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabelDto {
    private Long id;
    private Integer ordinal;
    @Pattern(regexp = "^.{1,30}$", message = "Title must be between 1 and 30 characters")
    private String title;
    @Pattern(regexp = "^.{1,30}$", message = "Content must be between 1 and 30 characters")
    private String content;
}
