package com.ringo.model.company;

import com.ringo.model.enums.Gender;
import com.ringo.model.photo.ParticipantPhoto;
import com.ringo.model.security.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "participant")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Participant extends User {

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @OneToOne
    @JoinColumn(name = "profile_picture")
    private ParticipantPhoto profilePicture;
}