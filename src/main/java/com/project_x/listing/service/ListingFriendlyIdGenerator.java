package com.project_x.listing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ListingFriendlyIdGenerator {

    public static final int SUFFIX_LENGTH = 16;

    // 32 symbols = exactly 5 bits per character. 0, 1, I and O are omitted.
    private static final char[] ALPHABET =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final Pattern VALID_PREFIX =
            Pattern.compile("[A-Z][A-Z0-9]{1,2}");

    private final SecureRandom secureRandom;
    private final String prefix;

    @Autowired
    public ListingFriendlyIdGenerator(
            @Value("${app.listing.friendly-id-prefix:PIL}") String prefix
    ) {
        this(prefix, new SecureRandom());
    }

    ListingFriendlyIdGenerator(String prefix, SecureRandom secureRandom) {
        this.prefix = normalizePrefix(prefix);
        this.secureRandom = secureRandom;
    }

    public String generate() {
        StringBuilder result = new StringBuilder(
                prefix.length() + 1 + SUFFIX_LENGTH
        );
        result.append(prefix).append('-');

        for (int index = 0; index < SUFFIX_LENGTH; index++) {
            result.append(ALPHABET[secureRandom.nextInt(ALPHABET.length)]);
        }

        return result.toString();
    }

    private static String normalizePrefix(String configuredPrefix) {
        if (configuredPrefix == null) {
            throw new IllegalArgumentException(
                    "app.listing.friendly-id-prefix must be configured"
            );
        }

        String normalized = configuredPrefix.trim().toUpperCase(Locale.ROOT);

        if (!VALID_PREFIX.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "app.listing.friendly-id-prefix must be 2-3 letters or digits " +
                            "and must start with a letter"
            );
        }

        return normalized;
    }
}
