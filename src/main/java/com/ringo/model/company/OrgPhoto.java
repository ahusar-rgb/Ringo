package com.ringo.model.company;

import com.ringo.model.common.AbstractActiveEntity;
import com.ringo.model.photo.Photo;
import jakarta.persistence.*;

@Entity
@Table(name = "organisation_photo")
public class OrgPhoto extends AbstractActiveEntity {

    @OneToOne
    @JoinColumn(name = "photo_id")
    private Photo photo;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation owner;
}
