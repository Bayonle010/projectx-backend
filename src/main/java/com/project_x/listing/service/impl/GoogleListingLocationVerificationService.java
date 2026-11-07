package com.project_x.listing.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project_x.listing.enums.LocationVerificationStatus;
import com.project_x.listing.service.ListingLocationVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;

@Slf4j
@Service
public class GoogleListingLocationVerificationService
        implements ListingLocationVerificationService {

    private final RestClient restClient;
    private final String apiKey;

    public GoogleListingLocationVerificationService(
            @Qualifier("googlePlacesRestClient") RestClient restClient,
            @Value("${google.maps.api-key:}") String apiKey
    ) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public VerificationResult verify(String placeId, Double suppliedLatitude, Double suppliedLongitude) {
        VerificationResult fallback = new VerificationResult(
                suppliedLatitude,
                suppliedLongitude,
                LocationVerificationStatus.UNVERIFIED,
                null
        );

        if (!StringUtils.hasText(placeId) || !StringUtils.hasText(apiKey)) {
            return fallback;
        }

        String safePlaceId = maskPlaceId(placeId);
        long startedAt = System.nanoTime();
        log.debug(
                "Google place verification request started: placeId={}",
                safePlaceId
        );

        try {
            PlaceDetailsResponse response = restClient.get()
                    .uri("/places/{placeId}", placeId.trim())
                    .header("X-Goog-Api-Key", apiKey.trim())
                    .header("X-Goog-FieldMask", "id,location")
                    .retrieve()
                    .body(PlaceDetailsResponse.class);

            if (response == null || response.location() == null || response.location().latitude() == null || response.location().longitude() == null) {
                log.debug(
                        "Google place verification response was incomplete: " + "placeId={}, durationMs={}",
                        safePlaceId,
                        elapsedMilliseconds(startedAt)
                );
                return new VerificationResult(
                        suppliedLatitude,
                        suppliedLongitude,
                        LocationVerificationStatus.VERIFICATION_FAILED,
                        null
                );
            }

            log.debug(
                    "Google place verification succeeded: placeId={}, durationMs={}",
                    safePlaceId,
                    elapsedMilliseconds(startedAt)
            );
            return new VerificationResult(
                    response.location().latitude(),
                    response.location().longitude(),
                    LocationVerificationStatus.VERIFIED,
                    Instant.now()
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.BAD_REQUEST || exception.getStatusCode() == HttpStatus.NOT_FOUND) {

                log.debug(
                        "Google rejected the place location: placeId={}, "
                                + "status={}, durationMs={}",
                        safePlaceId,
                        exception.getStatusCode().value(),
                        elapsedMilliseconds(startedAt)
                );
                return new VerificationResult(
                        suppliedLatitude,
                        suppliedLongitude,
                        LocationVerificationStatus.VERIFICATION_FAILED,
                        null
                );
            }

            logUnavailable(safePlaceId, startedAt, exception);
            return fallback;
        } catch (RuntimeException exception) {
            // Location verification enriches a listing; it must not make draft
            // saving or publication depend on Google availability.
            logUnavailable(safePlaceId, startedAt, exception);
            return fallback;
        }
    }

    private void logUnavailable(
            String safePlaceId,
            long startedAt,
            RuntimeException exception
    ) {
        log.warn(
                "Google place verification unavailable: placeId={}, "
                        + "durationMs={}, error={}",
                safePlaceId,
                elapsedMilliseconds(startedAt),
                exception.getMessage()
        );
    }

    private long elapsedMilliseconds(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String maskPlaceId(String placeId) {
        String normalized = placeId.trim();
        if (normalized.length() <= 8) {
            return "***";
        }
        return normalized.substring(0, 4)
                + "***"
                + normalized.substring(normalized.length() - 4);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaceDetailsResponse(String id, PlaceLocation location) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaceLocation(Double latitude, Double longitude) {
    }
}
