package com.project_x.review.service;

import com.project_x.core.exception.AccessDeniedException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.entity.ReviewComment;
import com.project_x.review.enums.ReviewCommentStatus;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.mapper.ReviewMapper;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewCommentLikeRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.service.impl.ReviewCommentServiceImpl;
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

class ReviewCommentDeleteServiceTests {

    @Test
    void commentAuthorCanSoftDeleteTheComment() {
        UUID authorId = UUID.randomUUID();
        ReviewComment comment = comment(authorId);
        AtomicBoolean saved = new AtomicBoolean();
        ReviewCommentServiceImpl service = service(
                Optional.of(comment),
                saved
        );

        service.delete(comment.getId(), auth(authorId));

        assertThat(saved).isTrue();
        assertThat(comment.getStatus())
                .isEqualTo(ReviewCommentStatus.DELETED);
    }

    @Test
    void anotherUserCannotDeleteTheComment() {
        ReviewComment comment = comment(UUID.randomUUID());
        AtomicBoolean saved = new AtomicBoolean();
        ReviewCommentServiceImpl service = service(
                Optional.of(comment),
                saved
        );

        assertThatThrownBy(() -> service.delete(
                comment.getId(),
                auth(UUID.randomUUID())
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only delete comments you created");
        assertThat(saved).isFalse();
        assertThat(comment.getStatus())
                .isEqualTo(ReviewCommentStatus.ACTIVE);
    }

    @Test
    void unavailableCommentCannotBeDeleted() {
        ReviewCommentServiceImpl service = service(
                Optional.empty(),
                new AtomicBoolean()
        );

        assertThatThrownBy(() -> service.delete(
                UUID.randomUUID(),
                auth(UUID.randomUUID())
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Review comment not found");
    }

    private ReviewCommentServiceImpl service(
            Optional<ReviewComment> comment,
            AtomicBoolean saved
    ) {
        PropertyReviewRepository reviewRepository = repository(
                PropertyReviewRepository.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType())
        );
        ReviewCommentRepository commentRepository = repository(
                ReviewCommentRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findByIdAndStatusAndReview_Status" -> comment;
                    case "save" -> {
                        saved.set(true);
                        yield arguments[0];
                    }
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewCommentLikeRepository likeRepository = repository(
                ReviewCommentLikeRepository.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType())
        );
        UserRepository userRepository = repository(
                UserRepository.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType())
        );

        return new ReviewCommentServiceImpl(
                reviewRepository,
                commentRepository,
                likeRepository,
                userRepository,
                new ReviewMapper()
        );
    }

    private ReviewComment comment(UUID authorId) {
        return ReviewComment.builder()
                .id(UUID.randomUUID())
                .review(PropertyReview.builder()
                        .id(UUID.randomUUID())
                        .status(ReviewStatus.ACTIVE)
                        .build())
                .author(User.builder()
                        .id(authorId)
                        .firstname("Test")
                        .lastname("Commenter")
                        .build())
                .content("A comment")
                .status(ReviewCommentStatus.ACTIVE)
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
