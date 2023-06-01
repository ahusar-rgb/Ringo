package com.ringo.model.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AbstractActiveEntity extends AbstractEntity{
    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE", nullable = false)
    private Boolean isActive;

    @Override
    @Transient
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AbstractActiveEntity that = (AbstractActiveEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }
}
