-- ProjectX listing-location database setup
--
-- Run this once for every PostgreSQL environment after the `listings` table
-- exists. The statements are idempotent, so the script is safe to run again.
--
-- Requirements:
--   1. PostgreSQL with PostGIS installed (for local Docker, use the
--      postgis/postgis image).
--   2. A database user allowed to create extensions and alter `listings`.

BEGIN;

CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE listings
    ADD COLUMN IF NOT EXISTS location_verification_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS location_verified_at TIMESTAMP WITH TIME ZONE;

UPDATE listings
SET location_verification_status = 'UNVERIFIED'
WHERE location_verification_status IS NULL;

ALTER TABLE listings
    ALTER COLUMN location_verification_status SET DEFAULT 'UNVERIFIED',
    ALTER COLUMN location_verification_status SET NOT NULL;

ALTER TABLE listings
    ADD COLUMN IF NOT EXISTS location geography(Point, 4326)
    GENERATED ALWAYS AS (
        CASE
            WHEN latitude IS NOT NULL AND longitude IS NOT NULL THEN
                ST_SetSRID(
                    ST_MakePoint(longitude, latitude),
                    4326
                )::geography
            ELSE NULL
        END
    ) STORED;

CREATE INDEX IF NOT EXISTS idx_listings_location_gist
    ON listings USING GIST (location)
    WHERE location IS NOT NULL;

COMMIT;

-- Verification queries:
--
-- SELECT PostGIS_Version();
--
-- SELECT column_name, data_type, is_nullable, column_default
-- FROM information_schema.columns
-- WHERE table_name = 'listings'
--   AND column_name IN (
--       'location',
--       'location_verification_status',
--       'location_verified_at'
--   );
--
-- SELECT indexname, indexdef
-- FROM pg_indexes
-- WHERE tablename = 'listings'
--   AND indexname = 'idx_listings_location_gist';
