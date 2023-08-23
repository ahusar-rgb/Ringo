package com.ringo.model.photo;

import com.ringo.model.common.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "photo")
public class Photo extends AbstractEntity {
    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "content_type", nullable = false)
    private String contentType;
}
