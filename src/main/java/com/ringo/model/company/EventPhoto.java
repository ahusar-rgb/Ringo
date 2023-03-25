package com.ringo.model.company;

import com.ringo.model.common.Photo;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
public class EventPhoto extends Photo {
    @OneToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
    private Event event;

    public EventPhoto(String path, Event event) {
        super(path);
        this.event = event;
    }
}
