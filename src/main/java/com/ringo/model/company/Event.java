package com.ringo.model.company;

import com.ringo.model.common.AbstractActiveEntity;
import com.ringo.model.form.RegistrationForm;
import com.ringo.model.photo.EventMainPhoto;
import com.ringo.model.photo.EventPhoto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "event")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Event extends AbstractActiveEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToOne(mappedBy = "event")
    private EventMainPhoto mainPhoto;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "ticket_needed", nullable = false)
    private Boolean isTicketNeeded;

    @OrderBy("ordinal ASC")
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TicketType> ticketTypes;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "finish_time")
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private Organisation host;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "events", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Category> categories;

    @OneToMany(mappedBy = "event")
    @Builder.Default
    @OrderBy("ordinal ASC")
    private List<EventPhoto> photos = new ArrayList<>();

    @Column(name = "photo_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer photoCount = 0;

    @Column(name = "people_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer peopleCount = 0;

    @Column(name = "people_saved", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer peopleSaved = 0;

    @ManyToMany(mappedBy = "savedEvents")
    private Set<Participant> savedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "registration_form", columnDefinition = "JSONB")
    private RegistrationForm registrationForm;

    @Override
    @Transient
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Event event = (Event) o;
        return getId() != null && Objects.equals(getId(), event.getId());
    }
}
