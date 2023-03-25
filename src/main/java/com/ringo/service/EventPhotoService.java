package com.ringo.service;

import com.ringo.dto.company.EventPhotoDto;
import com.ringo.exception.NotFoundException;
import com.ringo.mapper.company.EventPhotoMapper;
import com.ringo.repository.EventPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPhotoService {
    private final EventPhotoRepository repository;
    private final EventPhotoMapper mapper;

    public EventPhotoDto findPhotoById(Long id) {
        log.info("findPhotoById: {}", id);
        return mapper.toDto(repository.findById(id).orElseThrow(
                () -> new NotFoundException("Photo [id: %d] not found".formatted(id))
            )
        );
    }

    public List<EventPhotoDto> findPhotosByEventId(Long eventId) {
        log.info("findPhotosByEventId: {}", eventId);
        return mapper.toDtos(repository.findAllByEventId(eventId));
    }

    public EventPhotoDto findPhotoByPath(String path) {
        log.info("findPhotoByPath: {}", path);
        return mapper.toDto(repository.findByPath(path).orElseThrow(
                () -> new NotFoundException("Photo [path: %s] not found".formatted(path))
            )
        );
    }

    public EventPhotoDto savePhoto(EventPhotoDto photoDto) {
        return mapper.toDto(repository.save(mapper.toEntity(photoDto)));
    }

    public EventPhotoDto updatePhoto(EventPhotoDto photoDto) {
        if(repository.findById(photoDto.getId()).isEmpty())
            throw new NotFoundException("Photo [id: %d] not found".formatted(photoDto.getId()));
        return mapper.toDto(repository.save(mapper.toEntity(photoDto)));
    }

    public void deletePhoto(Long id) {
        if(repository.findById(id).isEmpty())
            throw new NotFoundException("Photo [id: %d] not found".formatted(id));
        repository.deleteById(id);
    }
}
