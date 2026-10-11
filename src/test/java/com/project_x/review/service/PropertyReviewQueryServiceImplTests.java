package com.project_x.review.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.review.dto.response.PropertyReviewResponse;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.mapper.ReviewMapper;
import com.project_x.review.projection.ReviewCountProjection;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.repository.ReviewLikeRepository;
import com.project_x.review.service.impl.PropertyReviewQueryServiceImpl;
import com.project_x.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyReviewQueryServiceImplTests {

    @Test
    void returnsReviewsToGuestsWithoutLookingUpUserLikes() {
        UUID listingId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        PropertyReview review = review(reviewId, listingId);
        AtomicBoolean userLikesQueried = new AtomicBoolean();

        PropertyReviewQueryServiceImpl queryService = queryService(
                review,
                reviewId,
                userLikesQueried,
                List.of()
        );

        Page<PropertyReviewResponse> response =
                queryService.getListingReviews(
                        listingId,
                        0L,
                        20L,
                        null
                );

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().likeCount()).isEqualTo(4);
        assertThat(response.getContent().getFirst().commentCount()).isEqualTo(2);
        assertThat(response.getContent().getFirst().likedByCurrentUser()).isFalse();
        assertThat(userLikesQueried).isFalse();
    }

    @Test
    void includesTheAuthenticatedUsersLikeState() {
        UUID listingId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PropertyReview review = review(reviewId, listingId);
        AtomicBoolean userLikesQueried = new AtomicBoolean();

        PropertyReviewQueryServiceImpl queryService = queryService(
                review,
                reviewId,
                userLikesQueried,
                List.of(reviewId)
        );
        AuthenticationIdentity auth = AuthenticationIdentity.builder()
                .id(userId.toString())
                .build();

        PropertyReviewResponse response = queryService.getListingReviews(
                listingId,
                0L,
                20L,
                auth
        ).getContent().getFirst();

        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(userLikesQueried).isTrue();
    }

    private PropertyReviewQueryServiceImpl queryService(
            PropertyReview review,
            UUID reviewId,
            AtomicBoolean userLikesQueried,
            List<UUID> likedReviewIds
    ) {
        PropertyReviewRepository reviewRepository = repository(
                PropertyReviewRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findByListing_IdAndStatusOrderByCreatedAtDesc" ->
                            new PageImpl<>(List.of(review));
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewLikeRepository likeRepository = repository(
                ReviewLikeRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "countLikesByReviewIds" -> List.of(count(reviewId, 4L));
                    case "findLikedReviewIds" -> {
                        userLikesQueried.set(true);
                        yield likedReviewIds;
                    }
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewCommentRepository commentRepository = repository(
                ReviewCommentRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "countCommentsByReviewIds" -> List.of(count(reviewId, 2L));
                    default -> defaultValue(method.getReturnType());
                }
        );
        ListingRepository listingRepository = repository(
                ListingRepository.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType())
        );

        return new PropertyReviewQueryServiceImpl(
                reviewRepository,
                likeRepository,
                commentRepository,
                listingRepository,
                new ReviewMapper()
        );
    }

    private PropertyReview review(UUID reviewId, UUID listingId) {
        return PropertyReview.builder()
                .id(reviewId)
                .listing(Listing.builder().id(listingId).build())
                .reviewer(User.builder()
                        .id(UUID.randomUUID())
                        .firstname("Test")
                        .lastname("Reviewer")
                        .build())
                .overallRating(4)
                .cleanlinessRating(4)
                .communicationRating(4)
                .accuracyRating(4)
                .valueForMoneyRating(4)
                .status(ReviewStatus.ACTIVE)
                .build();
    }

    private ReviewCountProjection count(UUID reviewId, Long total) {
        return new ReviewCountProjection() {
            @Override
            public UUID getReviewId() {
                return reviewId;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
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
        return 0;
    }
}
