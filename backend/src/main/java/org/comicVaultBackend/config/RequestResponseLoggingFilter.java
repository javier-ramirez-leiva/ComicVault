package org.comicVaultBackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

@Component
public class RequestResponseLoggingFilter implements Filter {

    @Autowired
    private List<HandlerMapping> handlerMappings;

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest((HttpServletRequest) request);
        CachedBodyHttpServletResponse cachedResponse = new CachedBodyHttpServletResponse((HttpServletResponse) response);
        chain.doFilter(cachedRequest, cachedResponse);

        if (!loggingInterceptor.isSkipLogginRequest()) {
            logRequest(cachedRequest, !loggingInterceptor.isSkipLogginRequestBody());
        }

        if (!loggingInterceptor.isSkipLoggingResponse()) {
            logResponse(cachedResponse, !loggingInterceptor.isSkipLoggingResponseBody());
        }


        cachedResponse.copyBodyToResponse(); // Important: restore body to actual response
    }

    private void logRequest(HttpServletRequest request, boolean includeBody) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n⬅️ [REQUEST] ").append(request.getMethod()).append(" ").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }

        sb.append("\nHeaders:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append("\n  ").append(name).append(": ").append(request.getHeader(name));
        }

        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        sb.append("\nBody:\n").append(body);

        logger.info(sb.toString());
    }

    private void logResponse(CachedBodyHttpServletResponse response, boolean includeBody) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n➡️ [RESPONSE] Status: ").append(response.getStatus());

        sb.append("\nHeaders:");
        for (String name : response.getHeaderNames()) {
            sb.append("\n  ").append(name).append(": ").append(response.getHeader(name));
        }

        if (includeBody) {
            String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
            sb.append("\nBody:\n").append(body);
        }


        logger.info(sb.toString());
    }
}
