package com.payme.token.exception;

import com.payme.internal.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgrumentNotValidException(
            HttpServletRequest request,
            MethodArgumentNotValidException exception
    ){
        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                        )
                );
        return ResponseEntity.badRequest().body(errors);
    }

    private ErrorResponse buildErrorResponse(
            RuntimeException runtimeException,
            HttpServletRequest request,
            HttpStatus status
    ){
        return ErrorResponse.builder()
                .errorTimestamp(LocalDateTime.now())
                .message(runtimeException.getMessage())
                .statusCode(status.value())
                .requestPath(request.getRequestURI())
                .build();
    }

}
