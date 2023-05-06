package com.ringo.model.company;

import com.ringo.model.common.AbstractEntity;
import com.ringo.model.photo.EventMainPhoto;
import com.ringo.model.photo.EventPhoto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Event extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private EventMainPhoto mainPhoto;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "ticket_needed", nullable = false)
    private Boolean isTicketNeeded;

    @Column(name = "price", nullable = false)
    private Float price;

    @OneToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "finish_time")
    private LocalDateTime endTime;

    @Column(name = "capacity")
    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private Organisation host;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable (
            name = "event_category",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "category_id"}))
    private List<Category> categories;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventPhoto> photos = new ArrayList<>();

    @Column(name = "people_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer peopleCount = 0;
}
