package com.ringo.repository.photo;

import com.ringo.model.photo.EventMainPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMainPhotoRepository extends JpaRepository<EventMainPhoto, Long> {
}
