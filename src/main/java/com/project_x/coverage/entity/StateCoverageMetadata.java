package com.project_x.coverage.entity;

import com.project_x.adress.entity.State;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "state_coverage_metadata",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_state_coverage_metadata_state",
                        columnNames = "state_id"
                ),
                @UniqueConstraint(
                        name = "uk_state_coverage_metadata_map_key",
                        columnNames = "map_key"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateCoverageMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "state_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_state_coverage_metadata_state"
            )
    )
    private State state;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "map_key", length = 80)
    private String mapKey;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
