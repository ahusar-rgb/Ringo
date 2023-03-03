package com.ringo.ticket;

import com.ringo.event.Event;
import com.ringo.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Table(name = "ticket")
@IdClass(TicketId.class)
public class Ticket {
    @Id
    private User user;
    @Id
    private Event event;
    @Column(name = "time_of_submission", nullable = false)
    private LocalDateTime timeOfSubmission;
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    @Column(name = "is_validated", nullable = false)
    private boolean isValidated;
}
