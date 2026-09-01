package com.project_x.listing.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListingFriendlyIdGeneratorTests {

    @Test
    void generatesUppercaseUnambiguousReferences() {
        ListingFriendlyIdGenerator generator =
                new ListingFriendlyIdGenerator("pil", new SecureRandom());

        Set<String> generated = new HashSet<>();
        for (int index = 0; index < 10_000; index++) {
            String friendlyId = generator.generate();

            assertTrue(friendlyId.matches("PIL-[2-9A-HJ-NP-Z]{16}"));
            generated.add(friendlyId);
        }

        assertEquals(10_000, generated.size());
    }

    @Test
    void rejectsInvalidPrefixes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ListingFriendlyIdGenerator("PROJECTX", new SecureRandom())
        );
    }
}
