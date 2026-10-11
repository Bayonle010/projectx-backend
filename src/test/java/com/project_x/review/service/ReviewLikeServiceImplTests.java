package com.project_x.review.service;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewLikeRepository;
import com.project_x.review.service.impl.ReviewLikeServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewLikeServiceImplTests {

    @Test
    void missingReviewReturnsResourceNotFound() {
        PropertyReviewRepository reviews = repository(
                PropertyReviewRepository.class,
                false
        );
        ReviewLikeRepository likes = repository(
                ReviewLikeRepository.class,
                null
        );
        ReviewLikeServiceImpl service =
                new ReviewLikeServiceImpl(reviews, likes);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.like(
                        UUID.randomUUID(),
                        AuthenticationIdentity.builder()
                                .id(UUID.randomUUID().toString())
                                .build()
                )
        );

        assertEquals(
                "Property review not found",
                exception.getMessage()
        );
    }

    @SuppressWarnings("unchecked")
    private <T> T repository(
            Class<T> repositoryType,
            Object returnValue
    ) {
        return (T) Proxy.newProxyInstance(
                repositoryType.getClassLoader(),
                new Class<?>[]{repositoryType},
                (proxy, method, arguments) -> returnValue
        );
    }
}
