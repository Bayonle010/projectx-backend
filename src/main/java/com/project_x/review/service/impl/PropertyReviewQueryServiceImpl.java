package com.project_x.review.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.review.dto.response.*;
import com.project_x.review.entity.PropertyReview;
import com.project_x.review.enums.ReviewCommentStatus;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.mapper.ReviewMapper;
import com.project_x.review.projection.*;
import com.project_x.review.repository.PropertyReviewRepository;
import com.project_x.review.repository.ReviewCommentRepository;
import com.project_x.review.repository.ReviewLikeRepository;
import com.project_x.review.service.PropertyReviewQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropertyReviewQueryServiceImpl
        implements PropertyReviewQueryService {

    private final PropertyReviewRepository reviewRepository;
    private final ReviewLikeRepository likeRepository;
    private final ReviewCommentRepository commentRepository;
    private final ListingRepository listingRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public PropertyReviewResponse getReview(
            UUID reviewId,
            AuthenticationIdentity auth
    ) {
        PropertyReview review = reviewRepository
                .findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Property review not found"
                ));

        long likeCount = likeRepository.countByReview_Id(reviewId);
        long commentCount = commentRepository
                .countByReview_IdAndStatus(
                        reviewId,
                        ReviewCommentStatus.ACTIVE
                );
        boolean likedByCurrentUser = auth != null
                && likeRepository.existsByReview_IdAndUser_Id(
                        reviewId,
                        UUID.fromString(auth.getId())
                );

        return reviewMapper.toResponse(
                review,
                likeCount,
                commentCount,
                likedByCurrentUser
        );
    }

    @Override
    public Page<PropertyReviewResponse> getListingReviews(
            UUID listingId,
            Long page,
            Long pageSize,
            AuthenticationIdentity auth
    ) {
        Pageable pageable =
                PaginationAdapters.createPageRequestWithRecentFirstsSortOrder(
                        page,
                        pageSize
                );

        Page<PropertyReview> reviewPage =
                reviewRepository
                        .findByListing_IdAndStatusOrderByCreatedAtDesc(
                                listingId,
                                ReviewStatus.ACTIVE,
                                pageable
                        );

        List<UUID> reviewIds = reviewPage.getContent()
                .stream()
                .map(PropertyReview::getId)
                .toList();

        if (reviewIds.isEmpty()) {
            return reviewPage.map(review ->
                    reviewMapper.toResponse(
                            review,
                            0,
                            0,
                            false
                    )
            );
        }

        Map<UUID, Long> likeCounts =
                likeRepository.countLikesByReviewIds(reviewIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ReviewCountProjection::getReviewId,
                                ReviewCountProjection::getTotal
                        ));

        Map<UUID, Long> commentCounts =
                commentRepository.countCommentsByReviewIds(
                                reviewIds,
                                ReviewCommentStatus.ACTIVE
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                ReviewCountProjection::getReviewId,
                                ReviewCountProjection::getTotal
                        ));

        Set<UUID> likedReviewIds = getLikedReviewIds(
                auth,
                reviewIds
        );

        return reviewPage.map(review -> {
            UUID reviewId = review.getId();

            return reviewMapper.toResponse(
                    review,
                    likeCounts.getOrDefault(reviewId, 0L),
                    commentCounts.getOrDefault(reviewId, 0L),
                    likedReviewIds.contains(reviewId)
            );
        });
    }

    @Override
    public ListingRatingSummaryResponse getListingSummary(
            UUID listingId
    ) {
        ListingRatingSummaryProjection summary =
                reviewRepository.getListingRatingSummary(
                        listingId,
                        ReviewStatus.ACTIVE
                );

        return new ListingRatingSummaryResponse(
                listingId,
                safeLong(summary.getTotalReviews()),
                reviewMapper.roundRating(summary.getAverageRating()),
                new RatingBreakdownResponse(
                        reviewMapper.roundRating(
                                summary.getCleanlinessRating()
                        ),
                        reviewMapper.roundRating(
                                summary.getCommunicationRating()
                        ),
                        reviewMapper.roundRating(
                                summary.getAccuracyRating()
                        ),
                        reviewMapper.roundRating(
                                summary.getValueForMoneyRating()
                        )
                )
        );
    }

    @Override
    public OwnerReviewDashboardResponse getOwnerDashboard(
            AuthenticationIdentity auth
    ) {
        UUID ownerId = UUID.fromString(auth.getId());

        long totalProperties =
                listingRepository.countByOwner_Id(ownerId);

        OwnerReviewAggregateProjection summary =
                reviewRepository.getOwnerReviewSummary(
                        ownerId,
                        ReviewStatus.ACTIVE
                );

        return new OwnerReviewDashboardResponse(
                totalProperties,
                safeLong(summary.getTotalReviews()),
                reviewMapper.roundRating(summary.getAverageRating()),
                new RatingBreakdownResponse(
                        reviewMapper.roundRating(
                                summary.getCleanlinessRating()
                        ),
                        reviewMapper.roundRating(
                                summary.getCommunicationRating()
                        ),
                        reviewMapper.roundRating(
                                summary.getAccuracyRating()
                        ),
                        reviewMapper.roundRating(
                                summary.getValueForMoneyRating()
                        )
                )
        );
    }

    @Override
    public Page<OwnerPropertyReviewResponse> getOwnerProperties(
            String search,
            Long page,
            Long pageSize,
            AuthenticationIdentity auth
    ) {
        Pageable pageable =
                PaginationAdapters.createPageRequestWithRecentFirstsSortOrder(
                        page,
                        pageSize
                );

        String normalizedSearch =
                search == null || search.isBlank()
                        ? ""
                        : search.trim();

        return reviewRepository
                .findOwnerPropertyReviewSummary(
                        UUID.fromString(auth.getId()),
                        ReviewStatus.ACTIVE,
                        normalizedSearch,
                        pageable
                )
                .map(projection ->
                        new OwnerPropertyReviewResponse(
                                projection.getListingId(),
                                projection.getFriendlyId(),
                                projection.getCoverImageUrl(),
                                projection.getAddress(),
                                safeLong(
                                        projection.getTotalReviews()
                                ),
                                reviewMapper.roundRating(
                                        projection.getAverageRating()
                                )
                        )
                );
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private Set<UUID> getLikedReviewIds(
            AuthenticationIdentity auth,
            List<UUID> reviewIds
    ) {
        if (auth == null) {
            return Collections.emptySet();
        }

        return new HashSet<>(
                likeRepository.findLikedReviewIds(
                        UUID.fromString(auth.getId()),
                        reviewIds
                )
        );
    }
}
