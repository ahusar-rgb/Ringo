package com.ringo.model.payment;

import com.ringo.model.common.AbstractEntity;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.model.company.TicketType;
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
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Table(name = "joining_intent")
public class JoiningIntent extends AbstractEntity {
    @OneToOne
    private Participant participant;

    @OneToOne
    private Event event;

    @OneToOne
    private TicketType ticketType;

    @Column(name = "payment_intent_id", unique = true)
    private String paymentIntentId;

    @Column(name = "payment_intent_client_secret")
    private String paymentIntentClientSecret;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JoiningIntentStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "registration_submission", columnDefinition = "JSONB")
    private RegistrationSubmission registrationSubmission;
}
