package com.project_x.coverage.repository;

import com.project_x.coverage.entity.StateCoverageMetadata;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface StateCoverageMetadataRepository
        extends JpaRepository<StateCoverageMetadata, UUID> {

    @EntityGraph(attributePaths = "state")
    List<StateCoverageMetadata> findByState_IdIn(
            Collection<UUID> stateIds
    );
}
