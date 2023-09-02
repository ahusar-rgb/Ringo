package com.ringo.model.payment;

import com.ringo.model.common.AbstractEntity;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
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
@Table(name = "stripe_payment")
public class JoiningIntent extends AbstractEntity {
    @OneToOne
    private Participant participant;

    @OneToOne
    private Event event;

    @Column(name = "payment_intent_id", nullable = false, unique = true)
    private String paymentIntentId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JoiningIntentStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "registration_submission", columnDefinition = "JSONB")
    private RegistrationSubmission registrationSubmission;
}
