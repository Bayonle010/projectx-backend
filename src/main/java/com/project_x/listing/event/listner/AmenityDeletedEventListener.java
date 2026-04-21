package com.project_x.listing.houseowners.event.listner;

import com.project_x.file.service.impl.CloudinaryServiceImpl;
import com.project_x.listing.houseowners.event.dto.AmenityDeletedEVent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AmenityDeletedEventListener {
    private final CloudinaryServiceImpl cloudinaryService;

    public AmenityDeletedEventListener(CloudinaryServiceImpl cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAmenityDeleted(AmenityDeletedEVent event) {
        if (event.publicId() != null && !event.publicId().isBlank()) {
            cloudinaryService.deleteFileByPublicId(event.publicId(), event.resourceType());
        }
    }
}
