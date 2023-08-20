package com.ringo.model.company;

import com.ringo.model.enums.Gender;
import com.ringo.model.security.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "participant")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Participant extends User {

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @ManyToMany
    @JoinTable(name = "event_save",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    @Builder.Default
    private Set<Event> savedEvents = new HashSet<>();

    @OneToMany(mappedBy = "participant", cascade = CascadeType.PERSIST)
    private List<Review> reviews;

    @PreRemove
    private void preRemove() {
        reviews.forEach(review -> review.setParticipant(null));
    }
}