package com.project_x.file;

import com.project_x.core.exception.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public final class UploadIdempotency {
    private UploadIdempotency() {
    }

    public static UUID parseKey(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Idempotency-Key header is required");
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Idempotency-Key must be a valid UUID");
        }
    }

    public static String fingerprint(String... parts) {
        return fingerprint(null, parts);
    }

    public static String fingerprint(byte[] content, String... parts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String part : parts) {
                String value = part == null ? "" : part;
                digest.update(value.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            if (content != null) {
                digest.update(content);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
