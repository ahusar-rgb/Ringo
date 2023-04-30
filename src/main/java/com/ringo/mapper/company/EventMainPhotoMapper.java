package com.ringo.mapper.company;

import com.ringo.dto.photo.EventMainPhotoDto;
import com.ringo.model.photo.EventMainPhoto;
import org.springframework.stereotype.Component;

@Component
public class EventMainPhotoMapper {
    public EventMainPhotoDto toDto(EventMainPhoto eventMainPhoto) {
        return EventMainPhotoDto.builder()
                .highQualityId(eventMainPhoto.getHighQualityPhoto().getId())
                .mediumQualityId(eventMainPhoto.getMediumQualityPhoto().getId())
                .lowQualityId(eventMainPhoto.getLowQualityPhoto().getId())
                .lazyId(eventMainPhoto.getLazyPhoto().getId())
                .build();
    }
}
