package com.ringo.model.company;

import com.ringo.model.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "category")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Category extends AbstractEntity {
    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name;


    @ManyToMany
    @JoinTable (
            name = "category_event",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "category_id"}))
    private Set<Event> events;
}
