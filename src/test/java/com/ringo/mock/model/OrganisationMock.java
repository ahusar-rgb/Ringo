package com.ringo.mock.model;

import com.ringo.it.util.IdGenerator;
import com.ringo.model.company.Organisation;
import com.ringo.model.security.Role;

import java.util.ArrayList;
import java.util.HashSet;

public class OrganisationMock {
    public static Organisation getOrganisationMock() {
        return Organisation.builder()
                .id(IdGenerator.getNewId())
                .isActive(true)
                .email("test@test.com")
                .name("Test")
                .username("test")
                .role(Role.ROLE_ORGANISATION)
                .description("Test description")
                .rating(null)
                .emailVerified(false)
                .withIdProvider(false)
                .contacts(new ArrayList<>())
                .hostedEvents(new HashSet<>())
                .reviews(new ArrayList<>())
                .build();
    }
}
