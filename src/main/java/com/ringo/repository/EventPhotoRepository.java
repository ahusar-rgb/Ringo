package com.ringo.repository;

import com.ringo.model.company.EventPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {
    @Query("SELECT ep FROM EventPhoto ep WHERE ep.path = :path")
    Optional<EventPhoto> findByPath(String path);
}
