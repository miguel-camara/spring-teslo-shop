package com.teslo.shop.common.exception;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        List<String> messages = ex
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(
                fe ->
                    fe.getField() +
                    ": " +
                    (fe.getDefaultMessage() != null
                        ? fe.getDefaultMessage()
                        : "is invalid")
            )
            .map(String::valueOf)
            .toList();
        return build(HttpStatus.BAD_REQUEST, messages);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request");
    }

    @ExceptionHandler(ApiBadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleApiBadRequest(
        ApiBadRequestException ex
    ) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ApiUnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleApiUnauthorized(
        ApiUnauthorizedException ex
    ) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({
        ApiForbiddenException.class,
        AccessDeniedException.class,
    })
    public ResponseEntity<Map<String, Object>> handleForbidden(Exception ex) {
        String message =
            ex instanceof ApiForbiddenException api
                ? api.getMessage()
                : "Forbidden";
        return build(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler(ApiNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
        ApiNotFoundException ex
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(
        AuthenticationException ex
    ) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
        HttpRequestMethodNotSupportedException ex
    ) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected error, check server logs"
        );
    }

    private ResponseEntity<Map<String, Object>> build(
        HttpStatus status,
        Object message
    ) {
        return ResponseEntity.status(status).body(
            Map.of(
                "statusCode",
                status.value(),
                "message",
                message,
                "error",
                status.getReasonPhrase()
            )
        );
    }
}
