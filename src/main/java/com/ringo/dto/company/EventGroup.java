package com.ringo.dto.company;

import com.ringo.dto.common.Coordinates;
import com.ringo.model.photo.EventMainPhoto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class EventGroup {
    private Coordinates coordinates;
    private Integer count;
    private EventMainPhoto mainPhoto;
    private Long id;
}
