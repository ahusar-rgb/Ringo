package com.ringo.dto.company;

import com.ringo.dto.common.AbstractEntityDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class CurrencyDto extends AbstractEntityDto {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Symbol is required")
    private Character symbol;
}
