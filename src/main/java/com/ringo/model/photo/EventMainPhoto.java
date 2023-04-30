package com.ringo.model.photo;

import com.ringo.model.common.AbstractEntity;
import com.ringo.model.company.Event;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "event_main_photo")
@Data
@SuperBuilder
@NoArgsConstructor
public class EventMainPhoto extends AbstractEntity {

    @OneToOne
    @JoinColumn(name = "high_quality_photo_id")
    private Photo highQualityPhoto;

    @OneToOne
    @JoinColumn(name = "medium_quality_photo_id")
    private Photo mediumQualityPhoto;

    @OneToOne
    @JoinColumn(name = "low_quality_photo_id")
    private Photo lowQualityPhoto;

    @OneToOne
    @JoinColumn(name = "lazy_photo_id")
    private Photo lazyPhoto;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}
