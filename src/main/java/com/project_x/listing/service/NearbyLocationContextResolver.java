package com.project_x.listing.service;

import java.util.UUID;

public interface NearbyLocationContextResolver {

    LocationContext resolve(double latitude, double longitude);

    record LocationContext(UUID stateId, UUID lgaId) {
        public static LocationContext unresolved() {
            return new LocationContext(null, null);
        }

        public boolean resolved() {
            return stateId != null;
        }
    }
}
