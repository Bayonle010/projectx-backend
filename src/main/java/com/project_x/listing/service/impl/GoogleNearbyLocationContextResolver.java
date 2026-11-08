package com.project_x.listing.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.adress.repository.LgaRepository;
import com.project_x.adress.repository.StateRepository;
import com.project_x.listing.service.NearbyLocationContextResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class GoogleNearbyLocationContextResolver
        implements NearbyLocationContextResolver {

    private static final int COORDINATE_CACHE_PRECISION = 1_000;
    private static final Set<String> STATE_TYPES = Set.of("administrative_area_level_1");
    private static final List<String> LGA_TYPE_PRIORITY = List.of(
            "administrative_area_level_2",
            "administrative_area_level_3",
            "locality"
    );

    private final RestClient restClient;
    private final StateRepository stateRepository;
    private final LgaRepository lgaRepository;
    private final String apiKey;
    private final long resolvedCacheTtlMs;
    private final long unresolvedCacheTtlMs;
    private final Map<CoordinateCacheKey, CacheEntry> cache;

    public GoogleNearbyLocationContextResolver(
            @Qualifier("googleGeocodingRestClient") RestClient restClient,
            StateRepository stateRepository,
            LgaRepository lgaRepository,
            @Value("${google.maps.api-key:}") String apiKey,
            @Value("${google.maps.location-cache-ttl-ms:3600000}") long resolvedCacheTtlMs,
            @Value("${google.maps.location-failure-cache-ttl-ms:300000}") long unresolvedCacheTtlMs,
            @Value("${google.maps.location-cache-max-entries:10000}") int maximumCacheEntries
    ) {
        this.restClient = restClient;
        this.stateRepository = stateRepository;
        this.lgaRepository = lgaRepository;
        this.apiKey = apiKey;
        this.resolvedCacheTtlMs = resolvedCacheTtlMs;
        this.unresolvedCacheTtlMs = unresolvedCacheTtlMs;
        this.cache = Collections.synchronizedMap(
                new LinkedHashMap<>(128, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(
                            Map.Entry<CoordinateCacheKey, CacheEntry> eldest
                    ) {
                        return size() > maximumCacheEntries;
                    }
                }
        );
    }

    @Override
    public LocationContext resolve(double latitude, double longitude) {
        CoordinateCacheKey cacheKey = CoordinateCacheKey.from(latitude, longitude);
        LocationContext cached = getCached(cacheKey);
        if (cached != null) {
            return cached;
        }

        LocationContext resolved = reverseGeocode(latitude, longitude);

        cache(
                cacheKey,
                resolved,
                resolved.resolved()
                        ? resolvedCacheTtlMs
                        : unresolvedCacheTtlMs
        );
        return resolved;
    }

    private LocationContext reverseGeocode(
            double latitude,
            double longitude
    ) {
        if (!StringUtils.hasText(apiKey)) {
            return LocationContext.unresolved();
        }

        try {
            GeocodeLocationResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/geocode/location")
                            .queryParam("location.latitude", latitude)
                            .queryParam("location.longitude", longitude)
                            .queryParam("languageCode", "en")
                            .build())
                    .header("X-Goog-Api-Key", apiKey.trim())
                    .header("X-Goog-FieldMask", "results.addressComponents.longText," + "results.addressComponents.shortText," + "results.addressComponents.types"
                    )
                    .retrieve()
                    .body(GeocodeLocationResponse.class);

            if (response == null || response.results() == null) {
                return LocationContext.unresolved();
            }

            List<AddressComponent> components = response.results().stream()
                    .filter(result -> result.addressComponents() != null)
                    .flatMap(result -> result.addressComponents().stream())
                    .toList();

            if (!isNigeria(components)) {
                return LocationContext.unresolved();
            }

            Optional<State> state = matchState(components);
            if (state.isEmpty()) {
                return LocationContext.unresolved();
            }

            Optional<Lga> lga = matchLga(components, state.get());

            log.debug(
                    "Nearby location context resolved: state={}, lga={}",
                    state.get().getName(),
                    lga.map(Lga::getName).orElse("unresolved")
            );

            return new LocationContext(
                    state.get().getId(),
                    lga.map(Lga::getId).orElse(null)
            );

        } catch (RuntimeException exception) {
            // Location context only improves result ordering. Property search
            // must still work when Google is unavailable or rejects a request.
            log.warn(
                    "Nearby location context could not be resolved: {}",
                    safeErrorDescription(exception)
            );
            return LocationContext.unresolved();
        }
    }

    private String safeErrorDescription(RuntimeException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            return "HTTP " + responseException.getStatusCode().value();
        }
        return exception.getClass().getSimpleName();
    }

    private boolean isNigeria(List<AddressComponent> components) {
        return components.stream()
                .filter(component -> hasType(component, "country"))
                .anyMatch(component ->
                        "ng".equals(normalizeName(component.shortText()))
                                || "nigeria".equals(
                                normalizeName(component.longText())
                        )
                );
    }

    private Optional<State> matchState(List<AddressComponent> components) {
        List<String> candidates = namesForTypes(components, STATE_TYPES);
        return stateRepository.findAllByOrderByNameAsc().stream()
                .filter(state -> matchesAny(state.getName(), candidates, true))
                .findFirst();
    }

    private Optional<Lga> matchLga(List<AddressComponent> components, State state) {
        List<Lga> lgas = lgaRepository.findByStateIdOrderByNameAsc(state.getId());

        for (String type : LGA_TYPE_PRIORITY) {
            List<String> candidates = namesForTypes(
                    components,
                    Set.of(type)
            );
            Optional<Lga> match = lgas.stream()
                    .filter(lga -> matchesAny(
                            lga.getName(),
                            candidates,
                            false
                    ))
                    .findFirst();
            if (match.isPresent()) {
                return match;
            }
        }

        return Optional.empty();
    }

    private List<String> namesForTypes(
            List<AddressComponent> components,
            Set<String> acceptedTypes
    ) {
        List<String> candidates = new ArrayList<>();
        for (AddressComponent component : components) {
            if (component.types() == null
                    || Collections.disjoint(
                    component.types(),
                    acceptedTypes
            )) {
                continue;
            }
            if (StringUtils.hasText(component.longText())) {
                candidates.add(component.longText());
            }
            if (StringUtils.hasText(component.shortText())) {
                candidates.add(component.shortText());
            }
        }
        return candidates;
    }

    private boolean matchesAny(
            String databaseName,
            List<String> candidates,
            boolean state
    ) {
        String normalizedDatabaseName = normalizeAdministrativeName(
                databaseName,
                state
        );
        return candidates.stream()
                .map(candidate -> normalizeAdministrativeName(candidate, state))
                .anyMatch(normalizedDatabaseName::equals);
    }

    private String normalizeAdministrativeName(String value, boolean state) {
        String normalized = normalizeName(value);
        if (state) {
            normalized = normalized.replaceFirst("\\s+state$", "");
        } else {
            normalized = normalized
                    .replaceFirst("\\s+local government area$", "")
                    .replaceFirst("\\s+lga$", "");
        }
        return normalized.trim();
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace("&", " and ")
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private boolean hasType(AddressComponent component, String type) {
        return component.types() != null && component.types().contains(type);
    }

    private LocationContext getCached(CoordinateCacheKey key) {
        synchronized (cache) {
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                return null;
            }
            if (entry.expiresAt().isBefore(Instant.now())) {
                cache.remove(key);
                return null;
            }
            return entry.context();
        }
    }

    private void cache(
            CoordinateCacheKey key,
            LocationContext context,
            long ttlMs
    ) {
        synchronized (cache) {
            cache.put(
                    key,
                    new CacheEntry(context, Instant.now().plusMillis(Math.max(ttlMs, 1)))
            );
        }
    }

    private record CoordinateCacheKey(long latitude, long longitude) {
        private static CoordinateCacheKey from(double latitude, double longitude) {
            return new CoordinateCacheKey(
                    Math.round(latitude * COORDINATE_CACHE_PRECISION),
                    Math.round(longitude * COORDINATE_CACHE_PRECISION)
            );
        }
    }

    private record CacheEntry(
            LocationContext context,
            Instant expiresAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodeLocationResponse(List<GeocodeResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodeResult(
            List<AddressComponent> addressComponents
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AddressComponent(
            String longText,
            String shortText,
            List<String> types
    ) {
    }
}
