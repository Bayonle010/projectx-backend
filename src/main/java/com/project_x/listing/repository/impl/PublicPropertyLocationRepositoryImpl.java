package com.project_x.listing.repository.impl;

import com.project_x.listing.dto.request.PublicPropertyFilterRequest;
import com.project_x.listing.enums.LocationMatchLevel;
import com.project_x.listing.repository.PublicPropertyLocationRepository;
import com.project_x.listing.repository.PublicPropertySearchHit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.sql.Types;

@Repository
public class PublicPropertyLocationRepositoryImpl
        implements PublicPropertyLocationRepository {

    private static final int BEDROOM_PLUS_THRESHOLD = 5;
    private static final int BATHROOM_PLUS_THRESHOLD = 5;
    private static final int TOILET_PLUS_THRESHOLD = 4;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final long nearbyRadiusMeters;

    public PublicPropertyLocationRepositoryImpl(
            NamedParameterJdbcTemplate jdbcTemplate,
            @Value("${app.listing.nearby-radius-meters:25000}") long nearbyRadiusMeters
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.nearbyRadiusMeters = nearbyRadiusMeters;
    }

    @Override
    public Page<PublicPropertySearchHit> search(
            PublicPropertyFilterRequest filter,
            Pageable pageable
    ) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        List<String> conditions = buildConditions(filter, parameters);
        String whereClause = " WHERE " + String.join(" AND ", conditions);
        boolean hasCoordinates = filter.getNearLatitude() != null;

        parameters.addValue("hasCoordinates", hasCoordinates);
        parameters.addValue("nearLatitude", filter.getNearLatitude(), Types.DOUBLE);
        parameters.addValue("nearLongitude", filter.getNearLongitude(), Types.DOUBLE);
        parameters.addValue("nearStateId", filter.getNearStateId(), Types.OTHER);
        parameters.addValue("nearLgaId", filter.getNearLgaId(), Types.OTHER);
        parameters.addValue("nearbyRadiusMeters", nearbyRadiusMeters);
        parameters.addValue("limit", pageable.getPageSize());
        parameters.addValue("offset", pageable.getOffset());

        String locationRank = """
                CASE
                    WHEN :hasCoordinates AND l.location IS NOT NULL AND
                        ST_DWithin(
                            l.location,
                            ST_SetSRID(
                                ST_MakePoint(:nearLongitude, :nearLatitude),
                                4326
                            )::geography,
                            :nearbyRadiusMeters
                        ) THEN 0
                    WHEN :nearLgaId IS NOT NULL AND l.lga_id = :nearLgaId THEN 1
                    WHEN :nearStateId IS NOT NULL AND l.state_id = :nearStateId THEN 2
                    ELSE 3
                END
                """;

        String distance = """
                CASE
                    WHEN :hasCoordinates AND l.location IS NOT NULL THEN
                        ST_Distance(
                            l.location,
                            ST_SetSRID(
                                ST_MakePoint(:nearLongitude, :nearLatitude),
                                4326
                            )::geography
                        )
                    ELSE NULL
                END
                """;

        String searchSql = "SELECT l.id, " + locationRank + " AS location_rank, "
                + distance + " AS distance_meters FROM listings l"
                + whereClause
                + " ORDER BY location_rank ASC, distance_meters ASC NULLS LAST, "
                + "l.created_at DESC, l.id DESC LIMIT :limit OFFSET :offset";

        List<PublicPropertySearchHit> hits = jdbcTemplate.query(
                searchSql,
                parameters,
                (resultSet, rowNumber) -> {
                    int rank = resultSet.getInt("location_rank");
                    double rawDistance = resultSet.getDouble("distance_meters");
                    Long roundedDistance = resultSet.wasNull()
                            ? null
                            : Math.round(rawDistance);

                    return new PublicPropertySearchHit(
                            resultSet.getObject("id", java.util.UUID.class),
                            roundedDistance,
                            toMatchLevel(rank)
                    );
                }
        );

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM listings l" + whereClause,
                parameters,
                Long.class
        );

        return new PageImpl<>(
                hits,
                pageable,
                total == null ? 0 : total
        );
    }

    private List<String> buildConditions(
            PublicPropertyFilterRequest filter,
            MapSqlParameterSource parameters
    ) {
        List<String> conditions = new ArrayList<>();
        conditions.add("l.status = 'PUBLISHED'");

        addEqualCondition(conditions, parameters, "l.state_id", "stateId", filter.getStateId());
        addEqualCondition(conditions, parameters, "l.lga_id", "lgaId", filter.getLgaId());

        if (hasText(filter.getNeighbourhood())) {
            conditions.add("LOWER(l.neighbourhood) LIKE :neighbourhood ESCAPE '!'");
            parameters.addValue(
                    "neighbourhood",
                    "%" + escapeLikePattern(
                            filter.getNeighbourhood().toLowerCase(Locale.ROOT)
                    ) + "%"
            );
        }

        if (hasValues(filter.getPropertyTypeIds())) {
            conditions.add("l.property_type_id IN (:propertyTypeIds)");
            parameters.addValue("propertyTypeIds", filter.getPropertyTypeIds());
        }

        Set<Integer> bedrooms = expandBedroomCounts(filter.getBedroomCounts());
        addCountCondition(
                conditions,
                parameters,
                "l.bedroom_count",
                "bedroomCounts",
                bedrooms,
                BEDROOM_PLUS_THRESHOLD
        );
        addCountCondition(
                conditions,
                parameters,
                "l.bathroom_count",
                "bathroomCounts",
                filter.getBathroomCounts(),
                BATHROOM_PLUS_THRESHOLD
        );
        addCountCondition(
                conditions,
                parameters,
                "l.toilet_count",
                "toiletCounts",
                filter.getToiletCounts(),
                TOILET_PLUS_THRESHOLD
        );

        if (filter.getMinRent() != null) {
            conditions.add("l.rent_amount >= :minRent");
            parameters.addValue("minRent", filter.getMinRent());
        }
        if (filter.getMaxRent() != null) {
            conditions.add("l.rent_amount <= :maxRent");
            parameters.addValue("maxRent", filter.getMaxRent());
        }

        return conditions;
    }

    private void addEqualCondition(
            List<String> conditions,
            MapSqlParameterSource parameters,
            String column,
            String parameterName,
            Object value
    ) {
        if (value != null) {
            conditions.add(column + " = :" + parameterName);
            parameters.addValue(parameterName, value);
        }
    }

    private void addCountCondition(
            List<String> conditions,
            MapSqlParameterSource parameters,
            String column,
            String parameterName,
            Set<Integer> selectedCounts,
            int plusThreshold
    ) {
        if (!hasValues(selectedCounts)) {
            return;
        }

        Set<Integer> exactCounts = new LinkedHashSet<>(selectedCounts);
        boolean includesPlus = exactCounts.remove(plusThreshold);
        List<String> alternatives = new ArrayList<>();

        if (!exactCounts.isEmpty()) {
            alternatives.add(column + " IN (:" + parameterName + ")");
            parameters.addValue(parameterName, exactCounts);
        }
        if (includesPlus) {
            String plusParameter = parameterName + "Plus";
            alternatives.add(column + " >= :" + plusParameter);
            parameters.addValue(plusParameter, plusThreshold);
        }

        conditions.add("(" + String.join(" OR ", alternatives) + ")");
    }

    private Set<Integer> expandBedroomCounts(Set<Integer> selectedCounts) {
        if (!hasValues(selectedCounts) || !selectedCounts.contains(1)) {
            return selectedCounts;
        }

        Set<Integer> expanded = new LinkedHashSet<>(selectedCounts);
        expanded.add(0);
        return expanded;
    }

    private LocationMatchLevel toMatchLevel(int rank) {
        return switch (rank) {
            case 0 -> LocationMatchLevel.EXACT;
            case 1 -> LocationMatchLevel.LGA;
            case 2 -> LocationMatchLevel.STATE;
            default -> LocationMatchLevel.NATIONWIDE;
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasValues(Set<?> values) {
        return values != null && !values.isEmpty();
    }

    private String escapeLikePattern(String value) {
        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}
