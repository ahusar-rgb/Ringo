package com.ringo.repository;

import com.ringo.model.company.Organisation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationRepository extends ActiveEntityRepository<Organisation> {

    @Query("SELECT o FROM Organisation o WHERE o.email = :email AND o.isActive = true")
    Optional<Organisation> findByEmail(String email);

    @Query("SELECT o FROM Organisation o WHERE o.username = :username AND o.isActive = true")
    Optional<Organisation> findByUsername(String username);
}
