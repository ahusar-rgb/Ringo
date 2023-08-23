package com.ringo.mapper.company;

import com.ringo.dto.photo.PhotoDto;
import com.ringo.model.photo.Photo;
import com.ringo.service.common.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhotoMapper {

    private final PhotoService photoService;

    public PhotoDto toDto(Photo photo) {
        return PhotoDto.builder()
                .id(photo.getId())
                .content(photoService.findBytes(photo.getId()))
                .path(photo.getPath())
                .contentType(photo.getContentType())
                .build();
    }
}
