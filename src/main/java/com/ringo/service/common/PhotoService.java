package com.ringo.service.common;

import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.model.photo.Photo;
import com.ringo.repository.photo.PhotoRepository;
import com.ringo.service.aws.s3.AwsFileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class PhotoService {
    protected final AwsFileManager awsFileManager;
    protected final PhotoRepository repository;

    public byte[] findBytes(Long id) {
        log.info("findPhoto: {}", id);
        Photo photo = repository.findById(id).orElseThrow(() -> new NotFoundException("Photo not found"));

        String path = photo.getPath();
        return awsFileManager.getFile(path);
    }

    public Photo findById(Long id) {
        log.info("findPhoto: {}", id);
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Photo not found"));
    }

    public Photo save(String path, String contentType, byte[] bytes) {
        log.info("savePhoto: {}", contentType);

        Photo photo = new Photo();

        if (contentType == null) {
            throw new UserException("Null file type");
        }
        photo.setContentType(contentType);
        photo.setPath(path);
        photo = repository.save(photo);

        awsFileManager.uploadFile(photo.getPath(), bytes);

        return photo;
    }

    public void delete(Long id) {
        log.info("deletePhoto: {}", id);

        Photo photo = repository.findById(id).orElseThrow(() -> new NotFoundException("Photo not found"));

        repository.delete(photo);
        awsFileManager.deleteFile(photo.getPath());
    }
}
