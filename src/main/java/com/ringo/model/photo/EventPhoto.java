package com.ringo.model.photo;

import com.ringo.model.common.AbstractEntity;
import com.ringo.model.company.Event;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "event_photo")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EventPhoto extends AbstractEntity {
    @OneToOne
    @JoinColumn(name = "photo_id")
    private Photo photo;

    @OneToOne
    @JoinColumn(name = "lazy_photo_id")
    private Photo lazyPhoto;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;
}
