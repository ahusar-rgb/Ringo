package com.ringo.service.company.event;

import com.ringo.exception.NotFoundException;
import com.ringo.model.common.AbstractEntity;
import com.ringo.model.company.Category;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCleanUpService {

    private final EventPhotoService eventPhotoService;

    public void cleanUpEvent(Event event) {
        if(event.getMainPhoto() != null)
            eventPhotoService.removeMainPhoto(event);

        event.getPhotos().stream().map(AbstractEntity::getId).forEach(
                photo -> {
                    try {
                        eventPhotoService.delete(photo);
                    } catch (NotFoundException ignored) {}
                }
        );
        
        for(Category category : event.getCategories()) {
            category.getEvents().remove(event);
        }

        for(Participant participant : event.getSavedBy()) {
            participant.getSavedEvents().remove(event);
        }
    }
}
