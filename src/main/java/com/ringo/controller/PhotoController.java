package com.ringo.controller;

import com.ringo.service.common.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/photos")
public class PhotoController {
    private final PhotoService photoService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> findEventPhotoById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(photoService.findBytes(id));
    }
}
