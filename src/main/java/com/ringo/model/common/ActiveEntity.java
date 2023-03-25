package com.ringo.model.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ActiveEntity extends AbstractEntity {

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;
}
