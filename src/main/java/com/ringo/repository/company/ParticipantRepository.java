package com.ringo.repository.company;

import com.ringo.model.company.Participant;
import com.ringo.repository.common.AbstractUserRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends AbstractUserRepository<Participant> {

    @Query("SELECT p FROM Participant p WHERE p.email = :email AND p.isActive = true")
    Optional<Participant> findByEmail(String email);

    @Query("SELECT p FROM Participant p WHERE p.isActive = true AND p.id = :id")
    Optional<Participant> findByIdActive(Long id);

    @Override
    @Query("SELECT p FROM Participant p LEFT JOIN FETCH p.savedEvents WHERE p.id = :id")
    Optional<Participant> findFullById(Long id);

    @Override
    @Query("SELECT p FROM Participant p LEFT JOIN FETCH p.savedEvents WHERE p.id = :id AND p.isActive = true")
    Optional<Participant> findFullActiveById(Long id);
}
