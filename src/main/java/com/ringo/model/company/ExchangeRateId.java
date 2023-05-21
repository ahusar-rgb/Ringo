package com.ringo.model.company;

import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ExchangeRateId implements Serializable {
    @OneToOne
    private Currency from;
    @OneToOne
    private Currency to;
}
