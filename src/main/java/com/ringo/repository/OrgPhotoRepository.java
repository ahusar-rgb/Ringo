package com.ringo.repository;

import com.ringo.model.company.OrgPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrgPhotoRepository extends JpaRepository<OrgPhoto, Long> {
    @Query("SELECT op FROM OrgPhoto op WHERE op.organisation.id = :orgId")
    List<OrgPhoto> findAllByOrgId(Long orgId);

    @Query("SELECT op FROM OrgPhoto op WHERE op.organisation.id = :orgId AND op.path = :path")
    Optional<OrgPhoto> findByOrgIdAndPath(Long orgId, String path);
}
