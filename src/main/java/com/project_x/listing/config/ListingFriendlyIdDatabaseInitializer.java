package com.project_x.listing.config;

import com.project_x.listing.service.ListingFriendlyIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Adds database-level protection and upgrades listings created before public
 * listing references were introduced.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ListingFriendlyIdDatabaseInitializer implements ApplicationRunner {

    private static final long MIGRATION_LOCK_ID = 7_241_190_031L;
    private static final int BATCH_SIZE = 1_000;
    private static final int MAX_ALLOCATION_ATTEMPTS = 10;

    private final JdbcTemplate jdbcTemplate;
    private final ListingFriendlyIdGenerator friendlyIdGenerator;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Serializes this upgrade when several application instances start.
        jdbcTemplate.execute(
                "SELECT pg_advisory_xact_lock(" + MIGRATION_LOCK_ID + ")"
        );
        jdbcTemplate.execute(
                "LOCK TABLE listings IN SHARE ROW EXCLUSIVE MODE"
        );

        // PostgreSQL unique indexes permit multiple nulls during the backfill.
        jdbcTemplate.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS uk_listings_friendly_id " +
                        "ON listings (friendly_id)"
        );

        backfillExistingListings();

        jdbcTemplate.execute(
                "ALTER TABLE listings ALTER COLUMN friendly_id SET NOT NULL"
        );
    }

    private void backfillExistingListings() {
        while (true) {
            List<UUID> listingIds = jdbcTemplate.queryForList(
                    "SELECT id FROM listings WHERE friendly_id IS NULL LIMIT " + BATCH_SIZE,
                    UUID.class
            );

            if (listingIds.isEmpty()) {
                return;
            }

            for (UUID listingId : listingIds) {
                allocateForExistingListing(listingId);
            }
        }
    }

    private void allocateForExistingListing(UUID listingId) {
        for (int attempt = 0; attempt < MAX_ALLOCATION_ATTEMPTS; attempt++) {
            String candidate = friendlyIdGenerator.generate();
            Boolean alreadyExists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (" +
                            "SELECT 1 FROM listings WHERE friendly_id = ?" +
                            ")",
                    Boolean.class,
                    candidate
            );

            if (Boolean.TRUE.equals(alreadyExists)) {
                continue;
            }

            int updated = jdbcTemplate.update(
                    "UPDATE listings SET friendly_id = ? " +
                            "WHERE id = ? AND friendly_id IS NULL",
                    candidate,
                    listingId
            );

            if (updated == 1) {
                return;
            }
        }

        throw new IllegalStateException(
                "Could not allocate a unique reference for listing " + listingId
        );
    }
}
