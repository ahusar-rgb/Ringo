package com.ringo.repository;

import com.ringo.model.company.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @Query("SELECT e FROM Event e WHERE e.host.id = :orgId")
    List<Event> findAllByOrgId(Long orgId);

    @Query("SELECT e FROM Event e " +
            "WHERE get_distance(e.latitude, e.longitude, :lat, :lon) < :distance")
    List<Event> findAllByDistance(double lat, double lon, double distance);

    @Query("SELECT e FROM Event e " +
            "ORDER BY get_distance(e.latitude, e.longitude, ?1, ?2) ASC")
    Page<Event> findTopByDistance(double lat, double lon, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.latitude < :latMax AND e.latitude > :latMin " +
            "AND e.longitude < :lonMax AND e.longitude > :lonMin")
    List<Event> findAllInArea(double latMin, double latMax, double lonMin, double lonMax);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.photos WHERE e.id = :id")
    Optional<Event> findById(Long id);
}
