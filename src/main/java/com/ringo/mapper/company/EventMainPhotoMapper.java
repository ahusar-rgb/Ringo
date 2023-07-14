package com.ringo.mapper.company;

import com.ringo.dto.photo.EventMainPhotoDto;
import com.ringo.model.photo.EventMainPhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMainPhotoMapper {

    @Mapping(target = "highQualityId", source = "highQualityPhoto.id")
    @Mapping(target = "mediumQualityId", source = "mediumQualityPhoto.id")
    @Mapping(target = "lowQualityId", source = "lowQualityPhoto.id")
    @Mapping(target = "lazyId", source = "lazyPhoto.id")
    EventMainPhotoDto toDto(EventMainPhoto eventMainPhoto);
}
