package com.ringo.model.company;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "event_id"})})
@Getter
@Setter
@IdClass(TicketId.class)
public class Ticket {
    @Id
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    @Id
    @OneToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
    private Event event;
    @Column(name = "time_of_submission", nullable = false)
    private LocalDateTime timeOfSubmission;
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    @Column(name = "is_validated", nullable = false)
    private boolean isValidated;
}
