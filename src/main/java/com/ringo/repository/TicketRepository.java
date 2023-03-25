package com.ringo.repository;

import com.ringo.model.company.Ticket;
import com.ringo.model.company.TicketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, TicketId> {
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId")
    List<Ticket> findAllByEventId(Long eventId);

    @Query("SELECT t FROM Ticket t WHERE t.user.id = :userId")
    List<Ticket> findAllByUserId(Long userId);
}
