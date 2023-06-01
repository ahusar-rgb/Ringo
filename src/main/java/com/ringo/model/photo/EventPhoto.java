package com.ringo.model.photo;

import com.ringo.model.common.AbstractEntity;
import com.ringo.model.company.Event;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "event_photo")
@Data
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

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;
}
