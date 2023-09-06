package com.ringo.model.company;

import com.ringo.model.form.RegistrationSubmission;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

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
    private Instant timeOfSubmission;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "is_validated", nullable = false)
    private Boolean isValidated;

    @Column(name = "is_paid", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPaid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "registration_submission", columnDefinition = "JSONB")
    private RegistrationSubmission registrationSubmission;
}
