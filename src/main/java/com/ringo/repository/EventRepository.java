package com.ringo.repository;

import com.ringo.model.company.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @Query("SELECT e FROM Event e " +
            "WHERE e.latitude < :latMax AND e.latitude > :latMin " +
            "AND e.longitude < :lonMax AND e.longitude > :lonMin " +
            "AND e.isActive")
    List<Event> findAllInArea(double latMin, double latMax, double lonMin, double lonMax);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.photos WHERE e.id = :id AND e.isActive")
    Optional<Event> findFullActiveById(Long id);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.photos WHERE e.id = :id")
    Optional<Event> findFullById(Long id);

    @Query("SELECT e FROM Event e WHERE e.id = :id AND e.isActive")
    Optional<Event> findActiveById(Long id);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.currency.id = :id")
    boolean existsByCurrencyId(Long id);
}
