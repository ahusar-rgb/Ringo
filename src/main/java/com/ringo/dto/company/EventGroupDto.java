package com.ringo.dto.company;

import com.ringo.dto.common.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class EventGroupDto {
    private Coordinates coordinates;
    private Integer count;
    private byte[] mainPhoto;
}
