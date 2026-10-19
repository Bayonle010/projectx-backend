package com.project_x.listing.repository;

import com.project_x.listing.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ListingImageRepository extends JpaRepository<ListingImage, UUID> {
    boolean existsByPublicId(String publicId);

    List<ListingImage> findByListing_IdInAndPosition(Collection<UUID> listingIds, Integer position);
}
