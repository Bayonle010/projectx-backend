package com.project_x.review.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.ReviewCommentLikeResponse;
import com.project_x.review.repository.ReviewCommentLikeRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.service.impl.ReviewCommentLikeServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCommentLikeServiceImplTests {

    @Test
    void likingACommentUsesAnIdempotentInsertAndReturnsTheCount() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AtomicBoolean insertAttempted = new AtomicBoolean();

        ReviewCommentRepository commentRepository = repository(
                ReviewCommentRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "existsByIdAndStatusAndReview_Status" -> true;
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewCommentLikeRepository likeRepository = repository(
                ReviewCommentLikeRepository.class,
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "insertIfAbsent" -> {
                        insertAttempted.set(true);
                        yield 1;
                    }
                    case "countByComment_Id" -> 5L;
                    default -> defaultValue(method.getReturnType());
                }
        );
        ReviewCommentLikeServiceImpl service =
                new ReviewCommentLikeServiceImpl(
                        commentRepository,
                        likeRepository
                );

        ReviewCommentLikeResponse response = service.like(
                commentId,
                AuthenticationIdentity.builder()
                        .id(userId.toString())
                        .build()
        );

        assertThat(insertAttempted).isTrue();
        assertThat(response.commentId()).isEqualTo(commentId);
        assertThat(response.liked()).isTrue();
        assertThat(response.totalLikes()).isEqualTo(5);
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
