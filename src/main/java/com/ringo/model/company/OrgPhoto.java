package com.ringo.model.company;

import com.ringo.model.common.Photo;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrgPhoto extends Photo {
    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;
}
