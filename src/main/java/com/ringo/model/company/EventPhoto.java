package com.ringo.model.company;

import com.ringo.model.common.Photo;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class EventPhoto extends Photo {
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}
