package com.project_x.listing.service.impl;

import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.adress.repository.LgaRepository;
import com.project_x.adress.repository.StateRepository;
import com.project_x.listing.service.NearbyLocationContextResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleNearbyLocationContextResolverTests {

    @Test
    void resolvesDatabaseStateAndLgaAndCachesNearbyCoordinates() {
        UUID stateId = UUID.randomUUID();
        UUID lgaId = UUID.randomUUID();
        State state = State.builder()
                .id(stateId)
                .name("Lagos")
                .build();
        Lga lga = Lga.builder()
                .id(lgaId)
                .name("Ikeja")
                .state(state)
                .build();

        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("https://geocode.googleapis.com/v4")
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                );
        MockRestServiceServer server = MockRestServiceServer
                .bindTo(restClientBuilder)
                .build();

        server.expect(request -> {
                    assertThat(request.getURI().getPath())
                            .isEqualTo("/v4/geocode/location");
                    assertThat(request.getURI().getQuery())
                            .contains("location.latitude=6.6018")
                            .contains("location.longitude=3.3515");
                })
                .andExpect(header("X-Goog-Api-Key", "test-key"))
                .andExpect(header(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andRespond(withSuccess(
                        """
                        {
                          "results": [{
                            "addressComponents": [
                              {
                                "longText": "Ikeja Local Government Area",
                                "shortText": "Ikeja",
                                "types": ["administrative_area_level_2", "political"]
                              },
                              {
                                "longText": "Lagos State",
                                "shortText": "LA",
                                "types": ["administrative_area_level_1", "political"]
                              },
                              {
                                "longText": "Nigeria",
                                "shortText": "NG",
                                "types": ["country", "political"]
                              }
                            ]
                          }]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        GoogleNearbyLocationContextResolver resolver =
                new GoogleNearbyLocationContextResolver(
                        restClientBuilder.build(),
                        repository(
                                StateRepository.class,
                                (proxy, method, arguments) ->
                                        method.getName().equals(
                                                "findAllByOrderByNameAsc"
                                        ) ? List.of(state) : defaultValue(
                                                method.getReturnType()
                                        )
                        ),
                        repository(
                                LgaRepository.class,
                                (proxy, method, arguments) ->
                                        method.getName().equals(
                                                "findByStateIdOrderByNameAsc"
                                        ) ? List.of(lga) : defaultValue(
                                                method.getReturnType()
                                        )
                        ),
                        "test-key",
                        3_600_000,
                        300_000,
                        100
                );

        NearbyLocationContextResolver.LocationContext first =
                resolver.resolve(6.6018, 3.3515);
        NearbyLocationContextResolver.LocationContext cached =
                resolver.resolve(6.60181, 3.35151);

        assertThat(first.stateId()).isEqualTo(stateId);
        assertThat(first.lgaId()).isEqualTo(lgaId);
        assertThat(cached).isEqualTo(first);
        server.verify();
    }

    @SuppressWarnings("unchecked")
    private <T> T repository(
            Class<T> repositoryType,
            InvocationHandler handler
    ) {
        return (T) Proxy.newProxyInstance(
                repositoryType.getClassLoader(),
                new Class<?>[]{repositoryType},
                handler
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        if (returnType == long.class) {
            return 0L;
        }
        return 0;
    }
}
