package com.ringo.user.organisation;

import com.ringo.event.Event;
import com.ringo.user.User;
import jakarta.persistence.*;

import java.util.Set;

@Table(name = "organisation")
public class Organisation {
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "host")
    private Set<Event> hostedEvents;

    @Column(name = "rating")
    private Float rating;

    @Column(name = "contacts")
    private String contacts;
}
