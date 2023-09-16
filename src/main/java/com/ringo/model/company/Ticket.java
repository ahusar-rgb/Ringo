package com.ringo.model.company;

import com.ringo.model.form.RegistrationSubmission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Ticket {
    @EmbeddedId
    private TicketId id;

    @Column(name = "time_of_submission", nullable = false)
    private LocalDateTime timeOfSubmission;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_validated", nullable = false)
    private Boolean isValidated;

    @Column(name = "is_paid", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPaid;

    @OneToOne
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "registration_submission", columnDefinition = "JSONB")
    private RegistrationSubmission registrationSubmission;
}
