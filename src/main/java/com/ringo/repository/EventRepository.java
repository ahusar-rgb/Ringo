package com.ringo.repository;

import com.ringo.model.company.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends ActiveEntityRepository<Event>, PagingAndSortingRepository<Event, Long> {
    @Query("SELECT e FROM Event e WHERE e.isActive AND e.host.id = :orgId")
    List<Event> findAllByOrgId(Long orgId);

    @Query("SELECT e FROM Event e " +
            "WHERE e.isActive " +
            "AND get_distance(e.latitude, e.longitude, :lat, :lon) < :distance")
    List<Event> findAllByDistance(double lat, double lon, double distance);

    @Query("SELECT e FROM Event e " +
            "WHERE e.isActive " +
            "AND e.latitude < :latMax AND e.latitude > :latMin " +
            "AND e.longitude < :lonMax AND e.longitude > :lonMin")
    List<Event> findAllInArea(double latMin, double latMax, double lonMin, double lonMax);

    @Override
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.photos WHERE e.id = :id")
    Optional<Event> findById(Long id);
}
