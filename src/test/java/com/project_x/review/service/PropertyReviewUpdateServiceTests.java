package com.project_x.review.service;

import com.project_x.core.exception.AccessDeniedException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.review.dto.request.SavePropertyReviewRequest;
import com.project_x.review.dto.response.PropertyReviewResponse;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.mapper.ReviewMapper;
import com.project_x.review.policy.ReviewEligibilityPolicy;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.repository.ReviewLikeRepository;
import com.project_x.review.service.impl.PropertyReviewCommandServiceImpl;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyReviewUpdateServiceTests {

    @Test
    void reviewAuthorCanUpdateTheReview() {
        UUID reviewId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        AtomicBoolean saved = new AtomicBoolean();
        PropertyReview review = review(reviewId, reviewerId);
        PropertyReviewCommandServiceImpl service = service(
                Optional.of(review),
                saved
        );

        PropertyReviewResponse response = service.update(
                reviewId,
                request(),
                auth(reviewerId)
        );

        assertThat(saved).isTrue();
        assertThat(response.overallRating()).isEqualTo(5);
        assertThat(response.ratingBreakdown().cleanliness()).isEqualTo(4);
        assertThat(response.ratingBreakdown().communication()).isEqualTo(3);
        assertThat(response.ratingBreakdown().accuracy()).isEqualTo(2);
        assertThat(response.ratingBreakdown().valueForMoney()).isEqualTo(1);
        assertThat(response.reviewText()).isNull();
        assertThat(response.likeCount()).isEqualTo(7);
        assertThat(response.commentCount()).isEqualTo(3);
        assertThat(response.likedByCurrentUser()).isTrue();
    }

    @Test
    void anotherUserCannotUpdateTheReview() {
        AtomicBoolean saved = new AtomicBoolean();
        PropertyReviewCommandServiceImpl service = service(
                Optional.of(review(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                )),
                saved
        );

        assertThatThrownBy(() -> service.update(
                UUID.randomUUID(),
                request(),
                auth(UUID.randomUUID())
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only edit reviews you created");
        assertThat(saved).isFalse();
    }

    @Test
    void unavailableReviewCannotBeUpdated() {
        PropertyReviewCommandServiceImpl service = service(
                Optional.empty(),
                new AtomicBoolean()
        );

        assertThatThrownBy(() -> service.update(
                UUID.randomUUID(),
                request(),
                auth(UUID.randomUUID())
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Property review not found");
    }

    private PropertyReviewCommandServiceImpl service(
            Optional<PropertyReview> review,
            AtomicBoolean saved
    ) {
        PropertyReviewRepository reviewRepository = repository(
                PropertyReviewRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findByIdAndStatus" -> review;
                    case "save" -> {
                        saved.set(true);
                        yield arguments[0];
                    }
                    default -> defaultValue(method.getReturnType());
                }
        );
        ListingRepository listingRepository = repository(
                ListingRepository.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType())
        );
        UserRepository userRepository = repository(
                UserRepository.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType())
        );
        ReviewLikeRepository likeRepository = repository(
                ReviewLikeRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "countByReview_Id" -> 7L;
                    case "existsByReview_IdAndUser_Id" -> true;
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewCommentRepository commentRepository = repository(
                ReviewCommentRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "countByReview_IdAndStatus" -> 3L;
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewEligibilityPolicy eligibilityPolicy = (listing, reviewerId) -> {
        };

        return new PropertyReviewCommandServiceImpl(
                reviewRepository,
                listingRepository,
                userRepository,
                eligibilityPolicy,
                new ReviewMapper(),
                likeRepository,
                commentRepository
        );
    }

    private PropertyReview review(UUID reviewId, UUID reviewerId) {
        return PropertyReview.builder()
                .id(reviewId)
                .listing(Listing.builder().id(UUID.randomUUID()).build())
                .reviewer(User.builder()
                        .id(reviewerId)
                        .firstname("Test")
                        .lastname("Reviewer")
                        .build())
                .overallRating(3)
                .cleanlinessRating(3)
                .communicationRating(3)
                .accuracyRating(3)
                .valueForMoneyRating(3)
                .reviewText("Old review")
                .status(ReviewStatus.ACTIVE)
                .build();
    }

    private SavePropertyReviewRequest request() {
        return new SavePropertyReviewRequest(
                5,
                4,
                3,
                2,
                1,
                "  "
        );
    }

    private AuthenticationIdentity auth(UUID userId) {
        return AuthenticationIdentity.builder()
                .id(userId.toString())
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
