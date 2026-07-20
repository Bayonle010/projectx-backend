package com.project_x.review.service.impl;

import com.project_x.core.exception.ConflictException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.review.dto.request.SavePropertyReviewRequest;
import com.project_x.review.dto.response.PropertyReviewResponse;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.mapper.ReviewMapper;
import com.project_x.review.policy.ReviewEligibilityPolicy;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.service.PropertyReviewCommandService;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyReviewCommandServiceImpl
        implements PropertyReviewCommandService {

    private final PropertyReviewRepository reviewRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ReviewEligibilityPolicy eligibilityPolicy;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public PropertyReviewResponse create(
            UUID listingId,
            SavePropertyReviewRequest request,
            AuthenticationIdentity auth
    ) {
        UUID reviewerId = UUID.fromString(auth.getId());

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Property listing not found"
                ));

        eligibilityPolicy.validate(listing, reviewerId);

        boolean alreadyReviewed =
                reviewRepository.existsByListing_IdAndReviewer_Id(
                        listingId,
                        reviewerId
                );

        if (alreadyReviewed) {
            throw new ConflictException(
                    "You have already reviewed this property"
            );
        }

        User reviewer = userRepository.getReferenceById(reviewerId);

        PropertyReview review = reviewMapper.toEntity(request);
        review.setListing(listing);
        review.setReviewer(reviewer);

        PropertyReview savedReview = reviewRepository.save(review);

        return reviewMapper.toResponse(
                savedReview,
                0,
                0,
                false
        );
    }
}