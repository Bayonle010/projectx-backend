package com.project_x.coverage.projection;

import java.util.UUID;

public interface StateCoverageCountProjection {

    UUID getStateId();

    String getStateName();

    Long getAvailableListingCount();
}
