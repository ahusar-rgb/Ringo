package com.ringo.model.company;

import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class TicketId implements Serializable {
    @OneToOne
    private Participant participant;

    @OneToOne
    private Event event;
}
