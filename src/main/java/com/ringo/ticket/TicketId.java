package com.ringo.ticket;

import com.ringo.event.Event;
import com.ringo.user.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@AllArgsConstructor
@EqualsAndHashCode
public class TicketId implements Serializable {
    private User user;
    private Event event;
}
