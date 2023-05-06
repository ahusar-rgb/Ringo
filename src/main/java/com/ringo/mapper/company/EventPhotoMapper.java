package com.ringo.mapper.company;

import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.model.photo.EventPhoto;
import org.springframework.stereotype.Component;

@Component
public class EventPhotoMapper {
    public EventPhotoDto toDto(EventPhoto eventPhoto) {
        return EventPhotoDto.builder()
                .id(eventPhoto.getId())
                .normalId(eventPhoto.getPhoto().getId())
                .lazyId(eventPhoto.getLazyPhoto().getId())
                .build();
    }
}
