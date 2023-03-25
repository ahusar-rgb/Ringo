package com.ringo.model.company;

import com.ringo.model.common.Photo;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "org_photo")
@Getter
@Setter
@NoArgsConstructor
public class OrgPhoto extends Photo {
    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id", nullable = false)
    private Organisation organisation;

    public OrgPhoto(String path, Organisation organisation) {
        super(path);
        this.organisation = organisation;
    }
}
