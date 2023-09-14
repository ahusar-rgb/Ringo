package com.ringo.repository.company;

import com.ringo.model.company.Organisation;
import com.ringo.repository.common.AbstractUserRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganisationRepository extends AbstractUserRepository<Organisation> {

    @Query("SELECT o FROM Organisation o LEFT JOIN FETCH o.hostedEvents WHERE o.id = :id AND o.isActive = true")
    Optional<Organisation> findByIdActiveWithEvents(Long id);

    @Query("SELECT o FROM Organisation o LEFT JOIN FETCH o.hostedEvents WHERE o.id = :id")
    Optional<Organisation> findByIdWithEvents(Long id);

    @Override
    @Query("SELECT o FROM Organisation o LEFT JOIN FETCH o.hostedEvents WHERE o.id = :id")
    Optional<Organisation> findFullById(Long id);

    @Query("SELECT o FROM Organisation o WHERE o.id = :id AND o.isActive = true")
    Optional<Organisation> findActiveById(Long id);

    @Override
    @Query("SELECT o FROM Organisation o LEFT JOIN FETCH o.hostedEvents WHERE o.id = :id AND o.isActive = true")
    Optional<Organisation> findFullActiveById(Long id);
}
