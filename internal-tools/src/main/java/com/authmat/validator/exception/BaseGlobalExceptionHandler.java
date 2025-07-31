package com.authmat.validator.exception;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BaseGlobalExceptionHandler
 *
 * A centralized abstract exception handler designed to provide consistent and secure
 * API error responses across all Spring Boot services. Intended to be extended by
 * service-specific {@code @RestControllerAdvice} classes.
 *
 * <p>This base class handles:
 * <ul>
 *   <li><b>404 Not Found</b> — via {@link org.springframework.web.servlet.NoHandlerFoundException}</li>
 *   <li><b>500 Internal Server Error</b> — via generic {@link java.lang.Exception}</li>
 * </ul>
 *
 * <p>Each service can subclass this to:
 * <ul>
 *   <li>Override default exception behavior</li>
 *   <li>Handle domain-specific exceptions</li>
 *   <li>Preserve a consistent JSON structure across APIs</li>
 * </ul>
 *
 * <p>The default error response includes:
 * <ul>
 *   <li>{@code status} – HTTP status code</li>
 *   <li>{@code error} – Reason phrase (e.g., "Not Found")</li>
 *   <li>{@code message} – Descriptive message</li>
 *   <li>{@code timestamp} – UTC timestamp of the error</li>
 * </ul>
 *
 * <p><b>Important:</b> To ensure {@link NoHandlerFoundException} is thrown for unmapped routes,
 * the following settings must be added to {@code application.yml} or {@code application.properties}:
 *
 * <pre>{@code
 * spring:
 *   mvc:
 *     throw-exception-if-no-handler-found: true
 *   web:
 *     resources:
 *       add-mappings: false
 * }</pre>
 *
 * <p>These settings:
 * <ul>
 *   <li>Ensure unmatched routes throw an exception instead of returning the default Whitelabel error page</li>
 *   <li>Prevent Spring Boot from silently handling static resource paths</li>
 * </ul>
 *
 * Recommended for use in microservice-based architectures and shared libraries where a predictable,
 * secure, and testable error handling strategy is required.
 */
@RestControllerAdvice
@Slf4j
public abstract class BaseGlobalExceptionHandler {


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundEndpoint(
            HttpServletRequest request,
            NoHandlerFoundException exception
    ){
        String requestUri = request.getRequestURI() != null ?
                request.getRequestURI() : "N/A";
        Sentry.captureException(exception);

        log.warn("404 Not Found: {}", requestUri);

        return generateErrorResponse(
                "The requested resource was not found",
                HttpStatus.NOT_FOUND,
                request
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception, HttpServletRequest request
    ){
        log.warn("Validation failed: {}", exception.getMessage());

        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (first, second) -> first
                        )
                );

        return generateErrorResponse(
                "Validation Failed.",
                HttpStatus.BAD_REQUEST,
                request,
                new HashMap<>(errors)
        );

    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
            Exception exception, HttpServletRequest request
    ){
        Sentry.captureException(exception);

        log.error("Unhandled exception occurred: {}", exception);

        return generateErrorResponse(
                "An unexpected error has occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }


    public ResponseEntity<ErrorResponse> generateErrorResponse(
            String message,
            HttpStatus status,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorTimestamp(LocalDateTime.now())
                .message(message)
                .requestPath(request.getRequestURI())
                .statusCode(status.value())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }


    public ResponseEntity<ErrorResponse> generateErrorResponse(
            String message,
            HttpStatus status,
            HttpServletRequest request,
            Object validationError
    ){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorTimestamp(LocalDateTime.now())
                .message(message.trim())
                .requestPath(request.getRequestURI())
                .statusCode(status.value())
                .validationErrors(validationError)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }


}
