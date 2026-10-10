package com.project_x.review.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.ReviewCommentResponse;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.entity.ReviewComment;
import com.project_x.review.mapper.ReviewMapper;
import com.project_x.review.projection.CommentCountProjection;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewCommentLikeRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.service.impl.ReviewCommentServiceImpl;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCommentServiceImplTests {

    @Test
    void returnsCommentLikeCountsToGuestsWithoutQueryingUserLikes() {
        UUID reviewId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        AtomicBoolean userLikesQueried = new AtomicBoolean();
        ReviewCommentServiceImpl service = service(
                comment(commentId, reviewId),
                commentId,
                userLikesQueried,
                List.of()
        );

        Page<ReviewCommentResponse> response = service.getComments(
                reviewId,
                0L,
                20L,
                null
        );

        ReviewCommentResponse comment = response.getContent().getFirst();
        assertThat(comment.likeCount()).isEqualTo(3);
        assertThat(comment.likedByCurrentUser()).isFalse();
        assertThat(userLikesQueried).isFalse();
    }

    @Test
    void includesTheAuthenticatedUsersCommentLikeState() {
        UUID reviewId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AtomicBoolean userLikesQueried = new AtomicBoolean();
        ReviewCommentServiceImpl service = service(
                comment(commentId, reviewId),
                commentId,
                userLikesQueried,
                List.of(commentId)
        );

        AuthenticationIdentity auth = AuthenticationIdentity.builder()
                .id(userId.toString())
                .build();

        ReviewCommentResponse response = service.getComments(
                reviewId,
                0L,
                20L,
                auth
        ).getContent().getFirst();

        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(userLikesQueried).isTrue();
    }

    private ReviewCommentServiceImpl service(
            ReviewComment comment,
            UUID commentId,
            AtomicBoolean userLikesQueried,
            List<UUID> likedCommentIds
    ) {
        PropertyReviewRepository reviewRepository = repository(
                PropertyReviewRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "existsByIdAndStatus" -> true;
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewCommentRepository commentRepository = repository(
                ReviewCommentRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "findByReview_IdAndStatusOrderByCreatedAtAsc" ->
                            new PageImpl<>(List.of(comment));
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewCommentLikeRepository likeRepository = repository(
                ReviewCommentLikeRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "countLikesByCommentIds" ->
                            List.of(count(commentId, 3L));
                    case "findLikedCommentIds" -> {
                        userLikesQueried.set(true);
                        yield likedCommentIds;
                    }
                    default -> defaultValue(method.getReturnType());
                }
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

    private ReviewComment comment(UUID commentId, UUID reviewId) {
        return ReviewComment.builder()
                .id(commentId)
                .review(PropertyReview.builder().id(reviewId).build())
                .author(User.builder()
                        .id(UUID.randomUUID())
                        .firstname("Test")
                        .lastname("Commenter")
                        .build())
                .content("Helpful review")
                .build();
    }

    private CommentCountProjection count(UUID commentId, Long total) {
        return new CommentCountProjection() {
            @Override
            public UUID getCommentId() {
                return commentId;
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
