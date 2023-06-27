package com.ringo.service.company.event;

import com.ringo.exception.InternalException;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.model.company.Event;
import com.ringo.model.photo.EventMainPhoto;
import com.ringo.model.photo.EventPhoto;
import com.ringo.model.photo.Photo;
import com.ringo.repository.photo.EventMainPhotoRepository;
import com.ringo.repository.photo.EventPhotoRepository;
import com.ringo.service.common.PhotoCompressor;
import com.ringo.service.common.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventPhotoService {

    private static final float HIGH_QUALITY = 0.75f;
    private static final float MEDIUM_QUALITY = 0.5f;
    private static final float LOW_QUALITY = 0.25f;

    private final PhotoService photoService;
    private final PhotoCompressor photoCompressor;
    private final EventPhotoRepository eventPhotoRepository;
    private final EventMainPhotoRepository eventMainPhotoRepository;

    public EventPhoto save(Event event, MultipartFile file) {
        EventPhoto eventPhoto = EventPhoto.builder()
                .event(event)
                .orderNumber(event.getPhotoCount() + 1)
                .build();

        String contentType = file.getContentType().split("/")[1];
        int ordinal = event.getPhotos() == null ? 0 : event.getPhotos().size();

        try {
            Photo highQualityPhoto = photoService.save(
                    "event#" + event.getId() +"/" + ordinal + "/normal." + contentType,
                    contentType,
                    photoCompressor.compressImage(file.getBytes(), contentType, HIGH_QUALITY)
            );
            eventPhoto.setPhoto(highQualityPhoto);
        } catch (IOException e) {
            throw new InternalException("Error while compressing photo");
        }

        try {
            Photo lazyPhoto = photoService.save(
                    "event#" + event.getId() +"/" + ordinal + "/lazy." + contentType,
                    contentType,
                    photoCompressor.createLazyPhoto(file.getBytes(), contentType)
            );
            eventPhoto.setLazyPhoto(lazyPhoto);
        }  catch (IOException e) {
            throw new InternalException("Error while creating lazy photo");
        }

        event.getPhotos().add(eventPhoto);
        return eventPhotoRepository.save(eventPhoto);
    }

    public void delete(Long photoId) {
        EventPhoto eventPhoto = eventPhotoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Photo not found"));

        eventPhotoRepository.delete(eventPhoto);
        photoService.delete(eventPhoto.getPhoto().getId());
        photoService.delete(eventPhoto.getLazyPhoto().getId());
    }

    public EventMainPhoto prepareMainPhoto(Event event, Long photoId) {
        EventPhoto eventPhoto = eventPhotoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Photo not found"));
        if(event.getPhotos() == null || !event.getPhotos().contains(eventPhoto)) {
            throw new UserException("Photo is not related to this event");
        }

        if(event.getMainPhoto() != null) {
            removeMainPhoto(event);
        }

        EventMainPhoto eventMainPhoto = EventMainPhoto.builder()
                .event(event)
                .highQualityPhoto(eventPhoto.getPhoto())
                .lazyPhoto(eventPhoto.getLazyPhoto())
                .build();

        byte[] bytes = photoService.findBytes(eventPhoto.getPhoto().getId());

        eventMainPhoto.setMediumQualityPhoto(saveCompressedPhoto(
                "event#" + event.getId() +"/main_photo/medium_quality." + eventPhoto.getPhoto().getContentType(),
                eventPhoto.getPhoto().getContentType(),
                bytes,
                MEDIUM_QUALITY));

        eventMainPhoto.setLowQualityPhoto(saveCompressedPhoto(
                "event#" + event.getId() +"/main_photo/low_quality." + eventPhoto.getPhoto().getContentType(),
                eventPhoto.getPhoto().getContentType(),
                bytes,
                LOW_QUALITY)
        );

        return eventMainPhotoRepository.save(eventMainPhoto);
    }

    public void removeMainPhoto(Event event) {
        if(event.getMainPhoto() == null) {
            throw new UserException("Event doesn't have main photo");
        }
        EventMainPhoto eventMainPhoto = event.getMainPhoto();

        eventMainPhotoRepository.delete(eventMainPhoto);
        photoService.delete(eventMainPhoto.getMediumQualityPhoto().getId());
        photoService.delete(eventMainPhoto.getLowQualityPhoto().getId());
    }

    private Photo saveCompressedPhoto(String path, String contentType, byte[] bytes, float quality) {
        try {
            return photoService.save(
                    path,
                    contentType,
                    photoCompressor.compressImage(bytes, contentType, quality)
            );
        } catch (IOException e) {
            throw new InternalException("Error while compressing photo");
        }
    }
}
