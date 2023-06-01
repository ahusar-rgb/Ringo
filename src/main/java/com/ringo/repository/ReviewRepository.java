package com.ringo.repository;

import com.ringo.model.company.Organisation;
import com.ringo.model.company.Participant;
import com.ringo.model.company.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT AVG(r.rate) FROM Review r WHERE r.organisation.id = :id")
    Float getAverageRatingByOrganisationId(Long id);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.organisation = :organisation AND r.participant = :participant")
    boolean existsByOrganisationAndParticipant(Organisation organisation, Participant participant);
}
