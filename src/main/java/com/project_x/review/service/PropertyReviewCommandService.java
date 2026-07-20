package com.project_x.review.service;


import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.request.SavePropertyReviewRequest;
import com.project_x.review.dto.response.PropertyReviewResponse;

import java.util.UUID;

public interface PropertyReviewCommandService {

    PropertyReviewResponse create(
            UUID listingId,
            SavePropertyReviewRequest request,
            AuthenticationIdentity auth
    );
}