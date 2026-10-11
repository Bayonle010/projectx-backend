package com.project_x.review.mapper;

import com.project_x.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReviewMapperTests {

    private final ReviewMapper mapper = new ReviewMapper();

    @Test
    void includesTheReviewerAvatarUrl() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstname("Adedibu")
                .lastname("Galatia")
                .avatar("https://cdn.example.com/avatar.jpg")
                .build();

        var response = mapper.toReviewerResponse(user);

        assertEquals("Adedibu Galatia", response.name());
        assertEquals(
                "https://cdn.example.com/avatar.jpg",
                response.avatarUrl()
        );
    }

    @Test
    void returnsNullWhenTheReviewerHasNoAvatar() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstname("Adedibu")
                .lastname("Galatia")
                .avatar("   ")
                .build();

        assertNull(
                mapper.toReviewerResponse(user).avatarUrl()
        );
    }
}
