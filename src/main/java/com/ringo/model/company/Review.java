package com.ringo.model.company;

import com.ringo.model.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "review",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "participant_id", "organisation_id" }) })
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Review extends AbstractEntity {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
