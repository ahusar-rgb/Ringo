package com.ringo.controller;

import com.ringo.exception.InternalException;
import com.ringo.model.photo.Photo;
import com.ringo.service.common.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/photos")
@Validated
public class PhotoController {
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> findEventPhotoById(@PathVariable("id") Long id) {
        Photo photo = photoService.findById(id);
        MediaType mediaType;
        if(photo.getContentType().equals("jpeg") || photo.getContentType().equals("jpg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if(photo.getContentType().equals("png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else {
            throw new InternalException("Unexpected value: " + photo.getContentType());
        }

        byte[] content = photoService.findBytes(id);
        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .contentLength(content.length)
                .body(content);
    }
}
