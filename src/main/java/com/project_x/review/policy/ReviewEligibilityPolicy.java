package com.project_x.review.policy;

import com.project_x.listing.entity.Listing;

import java.util.UUID;

public interface ReviewEligibilityPolicy {

    void validate(Listing listing, UUID reviewerId);
}