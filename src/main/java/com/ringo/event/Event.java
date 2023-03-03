package com.ringo.event;

import com.ringo.category.Category;
import com.ringo.currency.Currency;
import com.ringo.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "event")
public class Event {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "main_photo")
    private String pathToMainPhoto;

    @Column(name = "address")
    private String address;

    @Column(name = "coords", nullable = false)
    private String coords;

    @Column(name = "ticket_needed")
    private Boolean isTicketNeeded;

    @Column(name = "price")
    private Float price;

    @OneToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    @Column(name = "capacity")
    private Integer capacity;

    @ManyToMany
    @JoinTable (
            name = "joined_events",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;
}
