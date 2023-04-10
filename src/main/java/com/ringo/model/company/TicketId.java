package com.ringo.model.company;

import com.ringo.model.security.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class TicketId implements Serializable {
    private User user;
    private Event event;
}
