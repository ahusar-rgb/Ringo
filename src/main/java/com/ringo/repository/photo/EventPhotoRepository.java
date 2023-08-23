package com.ringo.repository.photo;

import com.ringo.model.photo.EventPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {
}
