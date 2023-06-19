package com.ringo.repository;

import com.ringo.model.company.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p FROM Participant p WHERE p.email = :email AND p.isActive = true")
    Optional<Participant> findByEmail(String email);

    @Query("SELECT p FROM Participant p WHERE p.username = :username AND p.isActive = true")
    Optional<Participant> findByUsername(String username);

    @Query("SELECT p FROM Participant p LEFT JOIN FETCH p.savedEvents WHERE p.id = :id AND p.isActive = true")
    Optional<Participant> findByIdWithSavedEvents(Long id);
}
