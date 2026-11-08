package com.project_x.core.filter;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves API routes before authentication so routing errors are independent
 * of whether a bearer token is missing, valid, or invalid.
 */
@Component
public class ApiRouteValidationFilter extends OncePerRequestFilter {

    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider;
    private final ObjectProvider<HandlerExceptionResolver> exceptionResolverProvider;

    public ApiRouteValidationFilter(
            @Qualifier("requestMappingHandlerMapping")
            ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider,
            @Qualifier("handlerExceptionResolver")
            ObjectProvider<HandlerExceptionResolver> exceptionResolverProvider
    ) {
        this.handlerMappingProvider = handlerMappingProvider;
        this.exceptionResolverProvider = exceptionResolverProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        RequestMappingHandlerMapping handlerMapping =
                handlerMappingProvider.getIfAvailable();

        if (handlerMapping == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            HandlerExecutionChain handler = handlerMapping.getHandler(request);

            if (handler == null) {
                resolveException(request, response,
                        new NoResourceFoundException(
                                HttpMethod.valueOf(request.getMethod()),
                                request.getRequestURI(),
                                "API endpoint"
                        )
                );
                return;
            }

            filterChain.doFilter(request, response);
        } catch (HttpRequestMethodNotSupportedException exception) {
            resolveException(request, response, exception);
        } catch (Exception exception) {
            resolveException(request, response, exception);
        }
    }

    private void resolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws ServletException {
        HandlerExceptionResolver exceptionResolver =
                exceptionResolverProvider.getIfAvailable();

        if (exceptionResolver == null) {
            throw new ServletException(exception);
        }

        ModelAndView resolution = exceptionResolver.resolveException(
                request,
                response,
                null,
                exception
        );

        if (resolution == null && !response.isCommitted()) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean apiRequest = uri.equals("/api") || uri.startsWith("/api/");

        return request.getDispatcherType() != DispatcherType.REQUEST
                || HttpMethod.OPTIONS.matches(request.getMethod())
                || !apiRequest;
    }
}
