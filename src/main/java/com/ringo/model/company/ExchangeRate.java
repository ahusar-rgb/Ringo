package com.ringo.model.company;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exchange_rates")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ExchangeRate {

    @EmbeddedId
    private ExchangeRateId id;

    @Column(name = "rate", nullable = false)
    private Float rate;
}
