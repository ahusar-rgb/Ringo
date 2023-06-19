package com.ringo.model.company;

import com.ringo.model.common.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "participant_id", "organisation_id" }) })
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Review extends AbstractAuditableEntity {

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "comment")
    private String comment;

    @Column(name = "rate", nullable = false)
    private Integer rate;
}
