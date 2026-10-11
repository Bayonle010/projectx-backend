package com.project_x.review.service;

import com.project_x.core.exception.AccessDeniedException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.repository.ListingRepository;
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

class PropertyReviewDeleteServiceTests {

    @Test
    void reviewAuthorCanSoftDeleteTheReview() {
        UUID reviewerId = UUID.randomUUID();
        PropertyReview review = review(reviewerId);
        AtomicBoolean saved = new AtomicBoolean();
        PropertyReviewCommandServiceImpl service = service(
                Optional.of(review),
                saved
        );

        service.delete(review.getId(), auth(reviewerId));

        assertThat(saved).isTrue();
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.DELETED);
    }

    @Test
    void anotherUserCannotDeleteTheReview() {
        PropertyReview review = review(UUID.randomUUID());
        AtomicBoolean saved = new AtomicBoolean();
        PropertyReviewCommandServiceImpl service = service(
                Optional.of(review),
                saved
        );

        assertThatThrownBy(() -> service.delete(
                review.getId(),
                auth(UUID.randomUUID())
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only delete reviews you created");
        assertThat(saved).isFalse();
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.ACTIVE);
    }

    @Test
    void unavailableReviewCannotBeDeleted() {
        PropertyReviewCommandServiceImpl service = service(
                Optional.empty(),
                new AtomicBoolean()
        );

        assertThatThrownBy(() -> service.delete(
                UUID.randomUUID(),
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
                (proxy, method, arguments) -> defaultValue(method.getReturnType())
        );
        ReviewCommentRepository commentRepository = repository(
                ReviewCommentRepository.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType())
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

    private PropertyReview review(UUID reviewerId) {
        return PropertyReview.builder()
                .id(UUID.randomUUID())
                .listing(Listing.builder().id(UUID.randomUUID()).build())
                .reviewer(User.builder()
                        .id(reviewerId)
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
