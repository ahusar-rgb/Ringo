package com.ringo.model.company;

import com.ringo.model.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "label")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Label extends AbstractEntity {
    @Column(name = "ordinal")
    private Integer ordinal;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "VARCHAR(255)")
    private String content;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;
}
