package com.ringo.mock.model;

import com.ringo.it.util.IdGenerator;
import com.ringo.model.photo.EventPhoto;
import com.ringo.model.photo.Photo;

public class EventPhotoMock {

    public static EventPhoto getEventPhotoMock() {
        return EventPhoto.builder()
                .id(IdGenerator.getNewId())
                .lazyPhoto(Photo.builder()
                        .id(IdGenerator.getNewId())
                        .path("testpath" + IdGenerator.getNewId())
                        .contentType("image/png")
                        .build())
                .photo(Photo.builder()
                        .id(IdGenerator.getNewId())
                        .path("testpath" + IdGenerator.getNewId())
                        .contentType("image/png")
                        .build())
                .build();
    }
}
