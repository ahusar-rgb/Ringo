package com.ringo.mapper.company;

import com.ringo.dto.photo.EventPhotoDto;
import com.ringo.model.photo.EventPhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventPhotoMapper {

    @Mapping(target = "normalId", source = "photo.id")
    @Mapping(target = "lazyId", source = "lazyPhoto.id")
    EventPhotoDto toDto(EventPhoto eventPhoto);
}
