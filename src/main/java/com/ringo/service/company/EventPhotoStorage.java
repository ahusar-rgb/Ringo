package com.ringo.service.company;

import com.ringo.dto.company.EventPhotoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPhotoStorage {
    private final EventPhotoService eventPhotoService;

    private static final String FOLDER_PATH = "/Users/aleksandr/RingoNew/Ringo/src/main/resources/photos/";

    public byte[] findPhoto(EventPhotoDto eventPhotoDto) {
        log.info("findPhoto: {}", eventPhotoDto);
        String path = eventPhotoDto.getPath();
        try {
            return Files.readAllBytes(new File(FOLDER_PATH + path).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EventPhotoDto savePhoto(EventPhotoDto eventPhotoDto, MultipartFile photo) {
        log.info("savePhoto: {}", eventPhotoDto);
        try {
            File file = new File(FOLDER_PATH + eventPhotoDto.getPath());
            Files.createDirectories(file.getParentFile().toPath());
            Files.createFile(file.toPath());
            photo.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return eventPhotoService.savePhoto(eventPhotoDto);
    }

    public EventPhotoDto deletePhoto(String path) {
        log.info("deletePhoto: {}", path);
        EventPhotoDto eventPhotoDto = eventPhotoService.findPhotoByPath(path);
        eventPhotoService.deletePhoto(eventPhotoDto.getId());

        File file = new File(FOLDER_PATH + path);
        if(!file.delete()) {
            log.error("File {} not deleted", path);
            throw new RuntimeException("File not deleted");
        }
        return eventPhotoDto;
    }
}
