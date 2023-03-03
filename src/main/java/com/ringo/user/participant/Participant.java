package com.ringo.user.participant;

import com.ringo.event.Event;
import com.ringo.user.Gender;
import com.ringo.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Table(name = "participant")
public class Participant {
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @ManyToMany
    @JoinTable (
            name = "joined_events",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> joinedEvents;

}