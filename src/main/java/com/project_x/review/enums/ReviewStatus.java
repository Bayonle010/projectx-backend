package com.project_x.review.enums;

public enum ReviewStatus {

    ACTIVE,

    /**
     * Hidden by an administrator or moderation process.
     */
    HIDDEN,

    /**
     * Soft-deleted by the reviewer.
     */
    DELETED
}