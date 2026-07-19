package com.project_x.review.enums;

public enum ReviewCommentStatus {

    ACTIVE,

    /**
     * Hidden by an administrator.
     */
    HIDDEN,

    /**
     * Soft-deleted by the comment author.
     */
    DELETED
}