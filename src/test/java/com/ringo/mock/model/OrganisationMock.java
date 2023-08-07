package com.ringo.mock.model;

import com.ringo.model.company.Organisation;
import com.ringo.model.security.Role;

import java.time.LocalDate;
import java.util.HashSet;

public class OrganisationMock {
    public static Organisation getOrganisationMock() {
        return Organisation.builder()
                .isActive(true)
                .email("test@test.com")
                .name("Test")
                .username("test")
                .role(Role.ROLE_ORGANISATION)
                .description("Test description")
                .rating(null)
                .contacts("Test contacts")
                .hostedEvents(new HashSet<>())
                .reviews(new HashSet<>())
                .build();
    }
}
