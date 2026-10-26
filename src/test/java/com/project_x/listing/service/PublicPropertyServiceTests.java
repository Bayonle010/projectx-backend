package com.project_x.listing.service;

import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.listing.builder.PublicPropertyResponseBuilder;
import com.project_x.listing.dto.response.PublicPropertyDetailsResponse;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.ListingImage;
import com.project_x.listing.entity.PropertyType;
import com.project_x.listing.enums.ListingStatus;
import com.project_x.listing.repository.ListingImageRepository;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.listing.service.impl.PublicPropertyServiceImpl;
import com.project_x.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PublicPropertyServiceTests {

    @Test
    void fetchesOnlyPublishedPropertiesAndLoadsCoversInOneBatch() {
        UUID listingId = UUID.randomUUID();
        Listing listing = listing(listingId);
        AtomicReference<ListingStatus> requestedStatus =
                new AtomicReference<>();
        AtomicReference<Pageable> requestedPageable =
                new AtomicReference<>();
        AtomicReference<List<UUID>> coverListingIds =
                new AtomicReference<>();
        AtomicInteger coverQueries = new AtomicInteger();

        ListingRepository listingRepository = repository(
                ListingRepository.class,
                (proxy, method, arguments) -> {
                    if (method.getName().equals("findByStatus")) {
                        requestedStatus.set((ListingStatus) arguments[0]);
                        requestedPageable.set((Pageable) arguments[1]);
                        return new PageImpl<>(
                                List.of(listing),
                                (Pageable) arguments[1],
                                1
                        );
                    }
                    return defaultValue(method.getReturnType());
                }
        );
        ListingImageRepository imageRepository = repository(
                ListingImageRepository.class,
                (proxy, method, arguments) -> {
                    if (method.getName().equals(
                            "findByListing_IdInAndPosition"
                    )) {
                        coverQueries.incrementAndGet();
                        coverListingIds.set(
                                List.copyOf((List<UUID>) arguments[0])
                        );
                        return List.of(ListingImage.builder()
                                .id(UUID.randomUUID())
                                .listing(listing)
                                .position(0)
                                .url("https://images.example/cover.jpg")
                                .publicId("cover")
                                .build());
                    }
                    return defaultValue(method.getReturnType());
                }
        );

        Page<PublicPropertyResponse> result =
                new PublicPropertyServiceImpl(
                        listingRepository,
                        imageRepository,
                        new PublicPropertyResponseBuilder()
                ).fetchPublishedProperties(1L, 20L);

        assertThat(requestedStatus.get())
                .isEqualTo(ListingStatus.PUBLISHED);
        assertThat(requestedPageable.get().getPageNumber()).isZero();
        assertThat(requestedPageable.get().getPageSize()).isEqualTo(20);
        assertThat(coverQueries.get()).isEqualTo(1);
        assertThat(coverListingIds.get()).containsExactly(listingId);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().coverImageUrl())
                .isEqualTo("https://images.example/cover.jpg");
    }

    @Test
    void fetchesOnlyPublishedPropertyAndHidesUnsharedExactAddress() {
        UUID listingId = UUID.randomUUID();
        Listing listing = listing(listingId);
        listing.setShareAddressWithSeekers(false);
        listing.setAddressLine("12 Private Street");
        listing.setLandmark("Private landmark");
        listing.setLatitude(6.6018);
        listing.setLongitude(3.3515);

        AtomicReference<UUID> requestedId = new AtomicReference<>();
        AtomicReference<ListingStatus> requestedStatus =
                new AtomicReference<>();

        ListingRepository listingRepository = repository(
                ListingRepository.class,
                (proxy, method, arguments) -> {
                    if (method.getName().equals("findByIdAndStatus")) {
                        requestedId.set((UUID) arguments[0]);
                        requestedStatus.set((ListingStatus) arguments[1]);
                        return Optional.of(listing);
                    }
                    return defaultValue(method.getReturnType());
                }
        );

        PublicPropertyDetailsResponse result =
                new PublicPropertyServiceImpl(
                        listingRepository,
                        repository(
                                ListingImageRepository.class,
                                (proxy, method, arguments) ->
                                        defaultValue(method.getReturnType())
                        ),
                        new PublicPropertyResponseBuilder()
                ).fetchPublishedProperty(listingId);

        assertThat(requestedId.get()).isEqualTo(listingId);
        assertThat(requestedStatus.get())
                .isEqualTo(ListingStatus.PUBLISHED);
        assertThat(result.exactAddressShared()).isFalse();
        assertThat(result.addressLine()).isNull();
        assertThat(result.landmark()).isNull();
        assertThat(result.latitude()).isNull();
        assertThat(result.longitude()).isNull();
    }

    private Listing listing(UUID listingId) {
        State state = State.builder()
                .id(UUID.randomUUID())
                .name("Lagos")
                .build();
        Lga lga = Lga.builder()
                .id(UUID.randomUUID())
                .name("Ikeja")
                .state(state)
                .build();
        PropertyType propertyType = PropertyType.builder()
                .id(UUID.randomUUID())
                .name("Apartment/Flat")
                .build();
        User owner = User.builder()
                .id(UUID.randomUUID())
                .firstname("Test")
                .lastname("Owner")
                .isEmailVerified(true)
                .build();

        return Listing.builder()
                .id(listingId)
                .friendlyId("PX-PUBLIC-1")
                .status(ListingStatus.PUBLISHED)
                .state(state)
                .lga(lga)
                .propertyType(propertyType)
                .owner(owner)
                .createdAt(Instant.now())
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
