package com.project_x.review.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PropertyReviewQueryService {

    Page<PropertyReviewResponse> getListingReviews(
            UUID listingId,
            Long page,
            Long pageSize,
            AuthenticationIdentity auth
    );

    ListingRatingSummaryResponse getListingSummary(
            UUID listingId
    );

    OwnerReviewDashboardResponse getOwnerDashboard(
            AuthenticationIdentity auth
    );

    Page<OwnerPropertyReviewResponse> getOwnerProperties(
            String search,
            Long page,
            Long pageSize,
            AuthenticationIdentity auth
    );
}