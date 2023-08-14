package com.ringo.model.company;

import com.ringo.model.security.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "organisation")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Organisation extends User {

    @Column(name = "description", columnDefinition = "VARCHAR(1000)")
    private String description;

    @Column(name = "rating")
    private Float rating;

    @OrderBy("ordinal")
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Label> contacts;

    @OneToMany(mappedBy = "host")
    @Builder.Default
    private Set<Event> hostedEvents = new HashSet<>();

    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Review> reviews = new HashSet<>();
}
