package com.project_x.listing.specification;

import com.project_x.listing.dto.request.PublicPropertyFilterRequest;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.enums.ListingStatus;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PublicPropertySpecification {

    private static final int BEDROOM_PLUS_THRESHOLD = 5;
    private static final int BATHROOM_PLUS_THRESHOLD = 5;
    private static final int TOILET_PLUS_THRESHOLD = 4;
    private static final char LIKE_ESCAPE_CHARACTER = '\\';

    private PublicPropertySpecification() {
    }

    public static Specification<Listing> matching(PublicPropertyFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            if (
                    query != null
                            && !Long.class.equals(query.getResultType())
                            && !long.class.equals(query.getResultType())
            ) {
                root.fetch("state", JoinType.LEFT);
                root.fetch("lga", JoinType.LEFT);
                root.fetch("owner", JoinType.LEFT);
                root.fetch("propertyType", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(
                    root.get("status"),
                    ListingStatus.PUBLISHED
            ));

            if (filter.getStateId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("state").get("id"),
                        filter.getStateId()
                ));
            }

            if (filter.getLgaId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("lga").get("id"),
                        filter.getLgaId()
                ));
            }

            if (hasText(filter.getNeighbourhood())) {
                String pattern = "%"
                        + escapeLikePattern(
                                filter.getNeighbourhood()
                                        .trim()
                                        .toLowerCase(Locale.ROOT)
                        )
                        + "%";

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("neighbourhood")),
                        pattern,
                        LIKE_ESCAPE_CHARACTER
                ));
            }

            if (hasValues(filter.getPropertyTypeIds())) {
                predicates.add(
                        root.get("propertyType")
                                .get("id")
                                .in(filter.getPropertyTypeIds())
                );
            }

            addCountPredicate(
                    predicates,
                    root.get("bedroomCount"),
                    expandBedroomCounts(filter.getBedroomCounts()),
                    BEDROOM_PLUS_THRESHOLD,
                    criteriaBuilder
            );

            addCountPredicate(
                    predicates,
                    root.get("bathroomCount"),
                    filter.getBathroomCounts(),
                    BATHROOM_PLUS_THRESHOLD,
                    criteriaBuilder
            );

            addCountPredicate(
                    predicates,
                    root.get("toiletCount"),
                    filter.getToiletCounts(),
                    TOILET_PLUS_THRESHOLD,
                    criteriaBuilder
            );

            if (filter.getMinRent() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("rentAmount"),
                        filter.getMinRent()
                ));
            }

            if (filter.getMaxRent() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("rentAmount"),
                        filter.getMaxRent()
                ));
            }

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }

    private static void addCountPredicate(
            List<Predicate> predicates,
            Expression<Integer> countExpression,
            Set<Integer> selectedCounts,
            int plusThreshold,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder
    ) {
        if (!hasValues(selectedCounts)) {
            return;
        }

        List<Predicate> countPredicates = selectedCounts.stream()
                .map(selectedCount ->
                        selectedCount == plusThreshold
                                ? criteriaBuilder.greaterThanOrEqualTo(
                                        countExpression,
                                        plusThreshold
                                )
                                : criteriaBuilder.equal(
                                        countExpression,
                                        selectedCount
                                )
                )
                .toList();

        predicates.add(criteriaBuilder.or(
                countPredicates.toArray(Predicate[]::new)
        ));
    }

    static Set<Integer> expandBedroomCounts(Set<Integer> selectedCounts) {
        if (!hasValues(selectedCounts) || !selectedCounts.contains(1)) {
            return selectedCounts;
        }

        Set<Integer> expandedCounts = new LinkedHashSet<>(selectedCounts);
        expandedCounts.add(0);
        return expandedCounts;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean hasValues(Set<?> values) {
        return values != null && !values.isEmpty();
    }

    private static String escapeLikePattern(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
