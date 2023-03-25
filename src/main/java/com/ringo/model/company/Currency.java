package com.ringo.model.company;

import com.ringo.model.common.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "currency")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Currency extends AbstractEntity {
    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name;
    @Column(name = "symbol", unique = true, nullable = false)
    private Character symbol;
}
