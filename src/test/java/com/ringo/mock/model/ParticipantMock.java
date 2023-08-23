package com.ringo.mock.model;

import com.ringo.it.util.IdGenerator;
import com.ringo.model.company.Participant;
import com.ringo.model.enums.Gender;
import com.ringo.model.security.Role;

import java.time.LocalDate;
import java.util.HashSet;

public class ParticipantMock {
    public static Participant getParticipantMock() {
        return Participant.builder()
                .id(IdGenerator.getNewId())
                .isActive(true)
                .email("test@test.com")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .name("Test")
                .username("test")
                .role(Role.ROLE_PARTICIPANT)
                .gender(Gender.MALE)
                .password("test")
                .emailVerified(false)
                .withIdProvider(false)
                .savedEvents(new HashSet<>())
                .build();
    }
}
