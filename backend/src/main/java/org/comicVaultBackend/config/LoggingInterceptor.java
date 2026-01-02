package org.comicVaultBackend.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.comicVaultBackend.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;
import java.util.OptionalLong;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private final ThreadLocal<Boolean> skipLoggingRequest = new ThreadLocal<>();
    private final ThreadLocal<Boolean> skipLoggingResponse = new ThreadLocal<>();
    private final ThreadLocal<Boolean> skipLoggingRequestBody = new ThreadLocal<>();
    private final ThreadLocal<Boolean> skipLoggingResponseBody = new ThreadLocal<>();
    private final ThreadLocal<StopWatch> callStopWatch = new ThreadLocal<>();

    private boolean logRequest;
    private boolean logResponse = false;

    @PostConstruct
    public void init() {
        String logEndpoint = System.getenv("LOG_ENDPOINT");
        logRequest = logEndpoint != null && (logEndpoint.equals("request") || logEndpoint.equals("all"));
        logResponse = logEndpoint != null && (logEndpoint.equals("response") || logEndpoint.equals("all"));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean shouldSkipRequest = true;
        boolean shouldSkipResponse = true;
        boolean shouldSkipResponseBody = true;
        boolean shouldSkipRequestBody = true;

        if (logRequest) {
            shouldSkipRequest = hasAnyDecorator(handler, SkipLogging.class, SkipLoggingRequest.class);
            shouldSkipRequestBody = hasAnyDecorator(handler, SkipLogging.class, SkipLoggingRequest.class, SkipLoggingRequestBody.class);
        }

        if (logResponse) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            callStopWatch.set(stopWatch);
            shouldSkipResponse = hasAnyDecorator(handler, SkipLogging.class, SkipLoggingResponse.class);
            shouldSkipResponseBody = hasAnyDecorator(handler, SkipLogging.class, SkipLoggingRequest.class, SkipLoggingResponseBody.class);
        }
        skipLoggingRequest.set(shouldSkipRequest);
        skipLoggingResponse.set(shouldSkipResponse);
        skipLoggingRequestBody.set(shouldSkipRequestBody);
        skipLoggingResponseBody.set(shouldSkipResponseBody);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        // If the response is to be logged, the stopwatch will be erased once we get the value later
        if (!skipLoggingResponse.get()) {
            callStopWatch.remove();
        }
        skipLoggingRequest.remove();
        skipLoggingResponse.remove();
        skipLoggingRequestBody.remove();
        skipLoggingResponseBody.remove();


    }

    private boolean hasAnyDecorator(Object handler, Class<? extends Annotation>... decoratorClasses) {
        if (handler instanceof HandlerMethod handlerMethod) {
            for (Class<? extends Annotation> decoratorClass : decoratorClasses) {
                if (handlerMethod.hasMethodAnnotation(decoratorClass) ||
                        handlerMethod.getBeanType().isAnnotationPresent(decoratorClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSkipLoggingResponse() {
        return Boolean.TRUE.equals(skipLoggingResponse.get());
    }

    public OptionalLong getElapsedTime() {
        StopWatch stopWatch = callStopWatch.get();
        callStopWatch.remove();
        if (stopWatch == null) {
            return OptionalLong.empty();
        }
        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
        return OptionalLong.of(stopWatch.getTotalTimeMillis());
    }

    public boolean isSkipLoggingResponseBody() {
        return Boolean.TRUE.equals(skipLoggingResponseBody.get());
    }

    public boolean isSkipLogginRequest() {
        return Boolean.TRUE.equals(skipLoggingRequest.get());
    }

    public boolean isSkipLogginRequestBody() {
        return Boolean.TRUE.equals(skipLoggingRequestBody.get());
    }
}
