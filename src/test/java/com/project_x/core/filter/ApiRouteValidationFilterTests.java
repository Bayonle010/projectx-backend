package com.project_x.core.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiRouteValidationFilterTests {

    @Test
    void delegatesUnknownApiEndpointToGlobalExceptionHandling()
            throws Exception {
        RequestMappingHandlerMapping handlerMapping =
                mock(RequestMappingHandlerMapping.class);
        HandlerExceptionResolver resolver = resolverReturningStatus(404);
        ApiRouteValidationFilter filter = filter(handlerMapping, resolver);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/not-an-endpoint"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(chain.getRequest()).isNull();
        verify(resolver).resolveException(
                any(),
                any(),
                isNull(),
                any(NoResourceFoundException.class)
        );
    }

    @Test
    void delegatesUnsupportedMethodToGlobalExceptionHandling()
            throws Exception {
        RequestMappingHandlerMapping handlerMapping =
                mock(RequestMappingHandlerMapping.class);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/available-properties"
        );
        when(handlerMapping.getHandler(request)).thenThrow(
                new HttpRequestMethodNotSupportedException("POST")
        );
        HandlerExceptionResolver resolver = resolverReturningStatus(405);
        ApiRouteValidationFilter filter = filter(handlerMapping, resolver);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(405);
        assertThat(chain.getRequest()).isNull();
        verify(resolver).resolveException(
                any(),
                any(),
                isNull(),
                any(HttpRequestMethodNotSupportedException.class)
        );
    }

    @Test
    void continuesToSecurityForExistingApiEndpoint() throws Exception {
        RequestMappingHandlerMapping handlerMapping =
                mock(RequestMappingHandlerMapping.class);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/listings"
        );
        when(handlerMapping.getHandler(request)).thenReturn(
                new HandlerExecutionChain(new Object())
        );
        HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
        ApiRouteValidationFilter filter = filter(handlerMapping, resolver);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(chain.getResponse()).isSameAs(response);
    }

    private HandlerExceptionResolver resolverReturningStatus(int status) {
        HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
        when(resolver.resolveException(any(), any(), isNull(), any()))
                .thenAnswer(invocation -> {
                    MockHttpServletResponse response = invocation.getArgument(1);
                    response.setStatus(status);
                    return new ModelAndView();
                });
        return resolver;
    }

    private ApiRouteValidationFilter filter(
            RequestMappingHandlerMapping handlerMapping,
            HandlerExceptionResolver exceptionResolver
    ) {
        return new ApiRouteValidationFilter(
                provider(handlerMapping),
                provider(exceptionResolver)
        );
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }
}
