package com.ringo.model.company;

import com.ringo.model.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Table(name = "ticket_type")
public class TicketType extends AbstractEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "VARCHAR(512)")
    private String description;

    @Column(name = "price", nullable = false)
    private Float price;

    @OneToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @Column(name = "sales_stop_time")
    private LocalDateTime salesStopTime;

    @Column(name = "people_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer peopleCount;

    @Column(name = "max_tickets")
    private Integer maxTickets;

    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

    @ManyToOne
    private Event event;
}
