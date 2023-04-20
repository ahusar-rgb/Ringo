package com.ringo.service.company;

import com.ringo.config.ApplicationProperties;
import com.ringo.model.company.Event;
import com.ringo.model.company.EventPhoto;
import com.ringo.repository.EventPhotoRepository;
import com.ringo.service.aws.s3.AwsFileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPhotoService {

    private final ApplicationProperties config;
    private final EventPhotoRepository eventPhotoRepository;

    private final AwsFileManager awsFileManager;

    public byte[] findBytes(EventPhoto eventPhoto) {
        log.info("findPhoto: {}", eventPhoto);

        String path = eventPhoto.getPath();
        try {
            //return Files.readAllBytes(new File(config.getPhotoFolderPath() + path).toPath());
            return awsFileManager.getFile(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    public EventPhoto savePhoto(Event event, MultipartFile photo) {
        log.info("savePhoto: {}, {}", event, photo.getOriginalFilename());

        EventPhoto eventPhoto = new EventPhoto();
        eventPhoto.setEvent(event);

        try {
            if(photo.getContentType() == null) {
                throw new RuntimeException("Null file type");
            }
            int ordinal;
            if(event.getPhotos() == null)
                ordinal = 0;
            else
                ordinal = event.getPhotos().size();
            String path = "event#" + event.getId() + "_" + ordinal + "." + photo.getContentType().split("/")[1];
//            File file = new File(config.getPhotoFolderPath() + path);
//            Files.createDirectories(file.getParentFile().toPath());
//            Files.createFile(file.toPath());
//            photo.transferTo(file);
            eventPhoto.setPath(path);
            awsFileManager.uploadFile(path, photo.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload the photo", e);
        }
        return eventPhotoRepository.save(eventPhoto);
    }

    public void deletePhoto(EventPhoto eventPhoto) {
        log.info("deletePhoto: {}", eventPhoto);

        eventPhotoRepository.deleteById(eventPhoto.getId());

//        File file = new File(config.getPhotoFolderPath() + eventPhoto.getPath());
//        if(!file.delete()) {
//            log.error("File {} not deleted", eventPhoto.getPath());
//            throw new RuntimeException("File not deleted");
//        }
        awsFileManager.deleteFile(eventPhoto.getPath());
    }
}
