package com.project_x.file;

public enum MediaKind {
    IMAGE("image", 5L * 1024 * 1024),
    VIDEO("video", 500L * 1024 * 1024),
    DOCUMENT("raw", 10L * 1024 * 1024);

    private final String resourceType;
    private final long maximumBytes;

    MediaKind(String resourceType, long maximumBytes) {
        this.resourceType = resourceType;
        this.maximumBytes = maximumBytes;
    }

    public String resourceType() {
        return resourceType;
    }

    public long maximumBytes() {
        return maximumBytes;
    }
}
