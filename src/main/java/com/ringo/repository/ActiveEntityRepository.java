package com.ringo.repository;

import com.ringo.model.common.AbstractActiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ActiveEntityRepository <T extends AbstractActiveEntity> extends JpaRepository<T, Long> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.isActive = true")
    Optional<T> findActiveById(Long id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.isActive = true")
    List<T> findAllActive();
}
