package com.ringo.dto.company;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRequestDto {
    private Long id;
    @Pattern(regexp = "^.{1,2000}$", message = "Comment must be between 1 and 2000 characters")
    private String comment;
    @Min(value = 1, message = "Rate must be between 1 and 5")
    @Max(value = 5, message = "Rate must be between 1 and 5")
    private Integer rate;
}
