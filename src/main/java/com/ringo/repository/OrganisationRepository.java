package com.ringo.repository;

import com.ringo.model.company.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    @Query("SELECT o FROM Organisation o WHERE o.email = :email AND o.isActive = true")
    Optional<Organisation> findByEmail(String email);

    @Query("SELECT o FROM Organisation o WHERE o.username = :username AND o.isActive = true")
    Optional<Organisation> findByUsername(String username);

    @Query("SELECT o FROM Organisation o LEFT JOIN FETCH o.hostedEvents WHERE o.id = :id AND o.isActive = true")
    Optional<Organisation> findByIdWithEvents(Long id);
}
