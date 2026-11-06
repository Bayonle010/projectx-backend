package com.project_x.listing.specification;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PublicPropertySpecificationTests {

    @Test
    void oneBedroomSelectionAlsoIncludesStudios() {
        Set<Integer> expandedCounts =
                PublicPropertySpecification.expandBedroomCounts(Set.of(1));

        assertThat(expandedCounts).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    void studioSelectionDoesNotIncludeOneBedroomProperties() {
        Set<Integer> expandedCounts =
                PublicPropertySpecification.expandBedroomCounts(Set.of(0));

        assertThat(expandedCounts).containsExactly(0);
    }
}
