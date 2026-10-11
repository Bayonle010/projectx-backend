package com.project_x.review.service;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.review.dto.response.PropertyReviewResponse;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.mapper.ReviewMapper;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.repository.ReviewLikeRepository;
import com.project_x.review.service.impl.PropertyReviewQueryServiceImpl;
import com.project_x.user.entity.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyReviewDetailsServiceTests {

    @Test
    void returnsReviewDetailsToGuestsWithoutLookingUpTheirLikeState() {
        UUID reviewId = UUID.randomUUID();
        AtomicBoolean userLikeQueried = new AtomicBoolean();
        PropertyReviewQueryServiceImpl queryService = queryService(
                Optional.of(review(reviewId)),
                userLikeQueried,
                false
        );

        PropertyReviewResponse response = queryService.getReview(
                reviewId,
                null
        );

        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.likeCount()).isEqualTo(4);
        assertThat(response.commentCount()).isEqualTo(2);
        assertThat(response.likedByCurrentUser()).isFalse();
        assertThat(response.reviewer().avatarUrl()).isNull();
        assertThat(userLikeQueried).isFalse();
    }

    @Test
    void includesTheAuthenticatedUsersLikeState() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AtomicBoolean userLikeQueried = new AtomicBoolean();
        PropertyReviewQueryServiceImpl queryService = queryService(
                Optional.of(review(reviewId)),
                userLikeQueried,
                true
        );
        AuthenticationIdentity auth = AuthenticationIdentity.builder()
                .id(userId.toString())
                .build();

        PropertyReviewResponse response = queryService.getReview(
                reviewId,
                auth
        );

        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(userLikeQueried).isTrue();
    }

    @Test
    void rejectsAnUnavailableReview() {
        UUID reviewId = UUID.randomUUID();
        PropertyReviewQueryServiceImpl queryService = queryService(
                Optional.empty(),
                new AtomicBoolean(),
                false
        );

        assertThatThrownBy(() -> queryService.getReview(reviewId, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Property review not found");
    }

    private PropertyReviewQueryServiceImpl queryService(
            Optional<PropertyReview> review,
            AtomicBoolean userLikeQueried,
            boolean likedByCurrentUser
    ) {
        PropertyReviewRepository reviewRepository = repository(
                PropertyReviewRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findByIdAndStatus" -> review;
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewLikeRepository likeRepository = repository(
                ReviewLikeRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "countByReview_Id" -> 4L;
                    case "existsByReview_IdAndUser_Id" -> {
                        userLikeQueried.set(true);
                        yield likedByCurrentUser;
                    }
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewCommentRepository commentRepository = repository(
                ReviewCommentRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "countByReview_IdAndStatus" -> 2L;
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

    private PropertyReview review(UUID reviewId) {
        return PropertyReview.builder()
                .id(reviewId)
                .listing(Listing.builder().id(UUID.randomUUID()).build())
                .reviewer(User.builder()
                        .id(UUID.randomUUID())
                        .firstname("Test")
                        .lastname("Reviewer")
                        .build())
                .overallRating(4)
                .cleanlinessRating(5)
                .communicationRating(4)
                .accuracyRating(4)
                .valueForMoneyRating(3)
                .reviewText("A useful review")
                .status(ReviewStatus.ACTIVE)
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
