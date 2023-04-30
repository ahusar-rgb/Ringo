package com.ringo.model.company;

import com.ringo.model.security.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organisation")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Organisation extends User {

    @OneToOne
    @JoinColumn(name = "profile_picture")
    private OrgPhoto profilePicture;

    @Column(name = "description", columnDefinition = "VARCHAR(1000)")
    private String description;

    @Column(name = "rating")
    private Float rating;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<OrgPhoto> photos = new HashSet<>();

    @Column(name = "contacts")
    private String contacts;

    @OneToMany(mappedBy = "host")
    @Builder.Default
    private Set<Event> hostedEvents = new HashSet<>();
}
