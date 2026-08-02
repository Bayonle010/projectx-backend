package com.project_x.review.policy;

import com.project_x.core.exception.AccessDeniedException;
import com.project_x.core.exception.BadRequestException;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.enums.ListingStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class DefaultReviewEligibilityPolicy
        implements ReviewEligibilityPolicy {

    @Override
    public void validate(Listing listing, UUID reviewerId) {

        if (listing.getStatus() != ListingStatus.PUBLISHED) {
            throw new BadRequestException(
                    "Only published properties can be reviewed"
            );
        }

        UUID ownerId = listing.getOwner().getId();

        if (Objects.equals(ownerId, reviewerId)) {
            throw new AccessDeniedException(
                    "You cannot review your own property"
            );
        }

        /*
         * Later:
         *
         * Verify that the reviewer actually rented this property.
         *
         * tenancyRepository.existsByListingIdAndTenantIdAndStatus(
         *     listing.getId(),
         *     reviewerId,
         *     TenancyStatus.COMPLETED
         * );
         */
    }
}