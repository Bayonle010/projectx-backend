package com.project_x.coverage.service;

import com.project_x.adress.entity.State;
import com.project_x.coverage.dto.CoverageStateResponse;
import com.project_x.coverage.entity.StateCoverageMetadata;
import com.project_x.coverage.projection.StateCoverageCountProjection;
import com.project_x.coverage.repository.StateCoverageMetadataRepository;
import com.project_x.listing.enums.ListingStatus;
import com.project_x.listing.repository.ListingRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CoverageServiceTests {

    @Test
    void derivesCoverageFromPublishedListingsAndAddsPresentationMetadata() {
        UUID lagosId = UUID.randomUUID();
        UUID fctId = UUID.randomUUID();
        AtomicReference<ListingStatus> requestedStatus =
                new AtomicReference<>();

        ListingRepository listingRepository = repository(
                ListingRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findStateCoverageByStatus" -> {
                        requestedStatus.set((ListingStatus) arguments[0]);
                        yield List.of(
                                count(lagosId, "Lagos", 427L),
                                count(
                                        fctId,
                                        "Federal Capital Territory",
                                        315L
                                )
                        );
                    }
                    default -> defaultValue(method.getReturnType());
                }
        );
        StateCoverageMetadataRepository metadataRepository = repository(
                StateCoverageMetadataRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findByState_IdIn" -> List.of(
                            metadata(
                                    fctId,
                                    "Abuja",
                                    "FCT",
                                    "https://images.example/abuja.jpg",
                                    1
                            ),
                            metadata(
                                    lagosId,
                                    "Lagos",
                                    "LAGOS",
                                    "https://images.example/lagos.jpg",
                                    2
                            )
                    );
                    default -> defaultValue(method.getReturnType());
                }
        );

        List<CoverageStateResponse> response = new CoverageService(
                listingRepository,
                metadataRepository
        ).getCoveredStates();

        assertThat(requestedStatus.get()).isEqualTo(ListingStatus.PUBLISHED);
        assertThat(response).hasSize(2);
        assertThat(response.getFirst().stateId()).isEqualTo(fctId);
        assertThat(response.getFirst().displayName()).isEqualTo("Abuja");
        assertThat(response.getFirst().mapKey()).isEqualTo("FCT");
        assertThat(response.getFirst().imageUrl())
                .isEqualTo("https://images.example/abuja.jpg");
        assertThat(response.getFirst().availableListingCount())
                .isEqualTo(315L);
        assertThat(response.get(1).stateId()).isEqualTo(lagosId);
    }

    @Test
    void returnsSafePresentationFallbacksWhenMetadataIsMissing() {
        UUID stateId = UUID.randomUUID();
        ListingRepository listingRepository = repository(
                ListingRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findStateCoverageByStatus" -> List.of(
                            count(stateId, "Cross River", 12L)
                    );
                    default -> defaultValue(method.getReturnType());
                }
        );
        StateCoverageMetadataRepository metadataRepository = repository(
                StateCoverageMetadataRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findByState_IdIn" -> List.of();
                    default -> defaultValue(method.getReturnType());
                }
        );

        CoverageStateResponse response = new CoverageService(
                listingRepository,
                metadataRepository
        ).getCoveredStates().getFirst();

        assertThat(response.displayName()).isEqualTo("Cross River");
        assertThat(response.mapKey()).isEqualTo("CROSS_RIVER");
        assertThat(response.imageUrl()).isNull();
        assertThat(response.availableListingCount()).isEqualTo(12L);
    }

    @Test
    void doesNotLoadMetadataWhenNoStateHasPublishedListings() {
        AtomicBoolean metadataQueried = new AtomicBoolean();
        ListingRepository listingRepository = repository(
                ListingRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findStateCoverageByStatus" -> List.of();
                    default -> defaultValue(method.getReturnType());
                }
        );
        StateCoverageMetadataRepository metadataRepository = repository(
                StateCoverageMetadataRepository.class,
                (proxy, method, arguments) -> {
                    if (method.getName().equals("findByState_IdIn")) {
                        metadataQueried.set(true);
                    }
                    return defaultValue(method.getReturnType());
                }
        );

        List<CoverageStateResponse> response = new CoverageService(
                listingRepository,
                metadataRepository
        ).getCoveredStates();

        assertThat(response).isEmpty();
        assertThat(metadataQueried).isFalse();
    }

    private StateCoverageCountProjection count(
            UUID stateId,
            String stateName,
            Long availableListingCount
    ) {
        return new StateCoverageCountProjection() {
            @Override
            public UUID getStateId() {
                return stateId;
            }

            @Override
            public String getStateName() {
                return stateName;
            }

            @Override
            public Long getAvailableListingCount() {
                return availableListingCount;
            }
        };
    }

    private StateCoverageMetadata metadata(
            UUID stateId,
            String displayName,
            String mapKey,
            String imageUrl,
            Integer displayOrder
    ) {
        return StateCoverageMetadata.builder()
                .id(UUID.randomUUID())
                .state(State.builder().id(stateId).build())
                .displayName(displayName)
                .mapKey(mapKey)
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> T repository(
            Class<T> repositoryType,
            InvocationHandler handler
    ) {
        return (T) Proxy.newProxyInstance(
                repositoryType.getClassLoader(),
                new Class<?>[]{repositoryType},
                handler
        );
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        if (returnType == long.class) {
            return 0L;
        }
        return 0;
    }
}
