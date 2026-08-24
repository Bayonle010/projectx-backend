package com.project_x.core.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.util.StringUtils;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(
            @Value("${http.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${http.response-timeout-ms:15000}") int responseTimeoutMs
    ) {
        return createRequestFactory(connectTimeoutMs, responseTimeoutMs);
    }

    private ClientHttpRequestFactory createRequestFactory(
            int connectTimeoutMs,
            int responseTimeoutMs
    ) {
        HttpClient jdk = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .version(HttpClient.Version.HTTP_2)
                .build();

        var jdkFactory = new JdkClientHttpRequestFactory(jdk);
        jdkFactory.setReadTimeout(Duration.ofMillis(responseTimeoutMs));

        return new BufferingClientHttpRequestFactory(jdkFactory);
    }

    @Bean
    public RestClient.Builder restClientBuilder(ClientHttpRequestFactory factory) {
        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                //  Predicate over HttpStatusCode, not (req, resp)
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        //  Error handler gets (HttpRequest, ClientHttpResponse)
                        (request, response) -> {
                            var status = response.getStatusCode();   // HttpStatusCode
                            var body   = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                            throw new RuntimeException("Upstream error " + status.value() + ": " + body);
                        }
                );
    }



    @Bean
    @Qualifier("zeptomailRestClient")
    public RestClient zeptoMailRestClient(RestClient.Builder builder,
                                          @Value("${zeptomail.base-url}") String baseUrl,
                                          @Value("${zeptomail.token}") String token) {
        return builder.clone()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, token);
                    return execution.execute(request, body);
                })
                .build();
    }

    @Bean
    @Qualifier("openAiRestClient")
    public RestClient openAiRestClient(
            RestClient.Builder builder,
            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.connect-timeout-ms:10000}") int connectTimeoutMs,
            @Value("${openai.response-timeout-ms:120000}") int responseTimeoutMs
    ) {
        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(
                        createRequestFactory(
                                connectTimeoutMs,
                                responseTimeoutMs
                        )
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .requestInterceptor((request, body, execution) -> {
                    if (StringUtils.hasText(apiKey)) {
                        request.getHeaders().setBearerAuth(apiKey.trim());
                    }

                    return execution.execute(request, body);
                })
                .build();
    }

}
