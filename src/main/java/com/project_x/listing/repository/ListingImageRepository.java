package com.project_x.listing.repository;

import com.project_x.listing.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ListingImageRepository extends JpaRepository<ListingImage, UUID> {
    boolean existsByPublicId(String publicId);
}
