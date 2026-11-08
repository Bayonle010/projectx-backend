package com.project_x.listing.repository;

import com.project_x.listing.dto.request.PublicPropertyFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublicPropertyLocationRepository {

    Page<PublicPropertySearchHit> search(PublicPropertyFilterRequest filter, Pageable pageable);
}
