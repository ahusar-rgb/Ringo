package com.ringo.unit;

import com.ringo.mock.model.EventMock;
import com.ringo.model.company.Event;
import com.ringo.model.photo.EventMainPhoto;
import com.ringo.model.photo.EventPhoto;
import com.ringo.service.company.event.EventCleanUpService;
import com.ringo.service.company.event.EventPhotoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EventCleanUpServiceTest {

    @Mock
    private EventPhotoService eventPhotoService;
    @InjectMocks
    private EventCleanUpService eventCleanUpService;

    @Test
    void cleanUpEventSuccess() {
        //given
        Event event = EventMock.getEventMock();
        event.setMainPhoto(new EventMainPhoto());
        event.setPhotos(List.of(EventPhoto.builder().id(1L).build(), EventPhoto.builder().id(2L).build()));

        //then
        eventCleanUpService.cleanUpEvent(event);

        verify(eventPhotoService, times(1)).removeMainPhoto(event);
        for(EventPhoto photo : event.getPhotos()) {
            verify(eventPhotoService, times(1)).delete(photo.getId());
        }
    }
}
