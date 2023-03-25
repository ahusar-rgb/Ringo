package com.ringo.repository;

import com.ringo.model.company.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends ActiveEntityRepository<Event> {
    @Query("SELECT e FROM Event e WHERE e.isActive AND e.host.id = :orgId")
    List<Event> findAllByOrgId(Long orgId);

    @Query("SELECT e FROM Event e " +
            "WHERE e.isActive " +
            "AND get_distance(e.latitude, e.longitude, :lat, :lon) < :distance")
    List<Event> findAllByDistance(double lat, double lon, double distance);
}
