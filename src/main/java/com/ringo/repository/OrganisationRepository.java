package com.ringo.repository;

import com.ringo.model.company.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    @Query("SELECT o FROM Organisation o WHERE o.email = :email")
    Optional<Organisation> findByEmail(String email);

    @Query("SELECT o FROM Organisation o WHERE o.username = :username")
    Optional<Organisation> findByUsername(String username);
}
