package com.ds.devsuaccount.infraestructure.config;

import com.ds.devsuaccount.infraestructure.exceptions.ApiErrorResponse;
import com.ds.devsuaccount.infraestructure.exceptions.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        log.error("ApiException: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ApiErrorResponse(ex));
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> noHandlerFoundException(
            HttpServletRequest req, NoHandlerFoundException ex) {
        log.warn("ApiException: {}", ex.getMessage());
        ApiException apiException = new ApiException(
                "route_not_found",
                String.format("Route %s not found", req.getRequestURI()),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity
                .status(apiException.getStatusCode())
                .body(new ApiErrorResponse(apiException));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> noResourceFoundException(
            HttpServletRequest req, NoResourceFoundException ex) {
        log.warn("ApiException: {}", ex.getMessage());
        ApiException apiException = new ApiException(
                "route_not_found",
                String.format("Route %s not found", req.getRequestURI()),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity
                .status(apiException.getStatusCode())
                .body(new ApiErrorResponse(apiException));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handleUnknownException(Exception e) {
        log.error("Internal error {}", e.getClass().getName(), e);

        if (e.getCause() instanceof ApiException apiEx) {
            return handleApiException(apiEx);
        }

        ApiException apiException = new ApiException(
                "internal_error",
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity
                .status(apiException.getStatusCode())
                .body(new ApiErrorResponse(apiException));
    }

}