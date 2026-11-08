package com.project_x.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public routes shared by the authorization rules and JWT filter.
 *
 * <p>The JWT filter must ignore these routes completely. Otherwise, an
 * expired token supplied to a {@code permitAll()} endpoint would be rejected
 * before Spring Security can apply the public authorization rule.</p>
 */
public final class PublicEndpointRegistry {

    private static final List<PublicEndpoint> PUBLIC_ENDPOINTS = List.of(
            any("/swagger-ui/**"),
            any("/api/v1/auth/**"),
            any("/login/**"),
            any("/oauth2/**"),
            any("/v2/api-docs"),
            any("/v3/api-docs/**"),
            any("/swagger-resources/**"),
            any("/configuration/ui"),
            any("/configuration/security"),
            any("/webjars/**"),
            any("/swagger-ui.html"),
            any("/ws/**"),
            get("/api/v1/reviews/listings/*"),
            get("/api/v1/reviews/listings/*/summary"),
            get("/api/v1/reviews/*"),
            get("/api/v1/reviews/*/comments"),
            get("/api/v1/coverage/**"),
            get("/api/v1/locations/**"),
            get("/api/v1/amenities"),
            get("/api/v1/amenities/*"),
            get("/api/v1/property-types"),
            get("/api/v1/property-types/*"),
            get("/api/v1/water-sources"),
            get("/api/v1/water-sources/*"),
            get("/api/v1/available-properties"),
            get("/api/v1/available-properties/**")
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private PublicEndpointRegistry() {
    }

    public static String[] endpointsForAnyMethod() {
        return PUBLIC_ENDPOINTS.stream()
                .filter(endpoint -> endpoint.method() == null)
                .map(PublicEndpoint::pattern)
                .toArray(String[]::new);
    }

    public static Map<HttpMethod, String[]> endpointsByMethod() {
        Map<HttpMethod, List<String>> groupedEndpoints = new LinkedHashMap<>();

        for (PublicEndpoint endpoint : PUBLIC_ENDPOINTS) {
            if (endpoint.method() == null) {
                continue;
            }
            groupedEndpoints.computeIfAbsent(
                    endpoint.method(),
                    ignored -> new java.util.ArrayList<>()
            ).add(endpoint.pattern());
        }

        Map<HttpMethod, String[]> result = new LinkedHashMap<>();
        groupedEndpoints.forEach((method, patterns) -> result.put(
                method,
                patterns.toArray(String[]::new)
        ));
        return Map.copyOf(result);
    }

    public static boolean isPublicRequest(HttpServletRequest request) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(endpoint ->
                (endpoint.method() == null
                        || endpoint.method().matches(request.getMethod()))
                        && PATH_MATCHER.match(
                        endpoint.pattern(),
                        request.getRequestURI()
                )
        );
    }

    private static PublicEndpoint get(String pattern) {
        return new PublicEndpoint(HttpMethod.GET, pattern);
    }

    private static PublicEndpoint any(String pattern) {
        return new PublicEndpoint(null, pattern);
    }

    private record PublicEndpoint(HttpMethod method, String pattern) {
    }
}
