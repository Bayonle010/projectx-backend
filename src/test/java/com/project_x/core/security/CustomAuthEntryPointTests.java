package com.project_x.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomAuthEntryPointTests {

    @Test
    void returnsNotFoundForUnknownApiEndpoint() throws Exception {
        RequestMappingHandlerMapping handlerMapping =
                mock(RequestMappingHandlerMapping.class);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/not-an-endpoint"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        CustomAuthEntryPoint entryPoint = entryPoint(handlerMapping);
        entryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Authentication required")
        );

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString()).contains(
                "The requested endpoint does not exist"
        );
    }

    @Test
    void returnsUnauthorizedForExistingProtectedApiEndpoint()
            throws Exception {
        RequestMappingHandlerMapping handlerMapping =
                mock(RequestMappingHandlerMapping.class);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/listings"
        );
        when(handlerMapping.getHandler(request)).thenReturn(
                new HandlerExecutionChain(new Object())
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        CustomAuthEntryPoint entryPoint = entryPoint(handlerMapping);
        entryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Authentication required")
        );

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @SuppressWarnings("unchecked")
    private CustomAuthEntryPoint entryPoint(
            RequestMappingHandlerMapping handlerMapping
    ) {
        ObjectProvider<RequestMappingHandlerMapping> provider =
                mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(handlerMapping);

        return new CustomAuthEntryPoint(new ObjectMapper(), provider);
    }
}
