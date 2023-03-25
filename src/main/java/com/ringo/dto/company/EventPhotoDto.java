package com.ringo.dto.company;

import com.ringo.dto.common.PhotoDto;
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
public class EventPhotoDto extends PhotoDto {
    private Boolean isMain;
    private Long eventId;
}
