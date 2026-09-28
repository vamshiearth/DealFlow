package com.dealflow.backend.common.exception;

import com.dealflow.backend.deal.DealNotFoundException;
import com.dealflow.backend.deal.InvalidDealTransitionException;
import com.dealflow.backend.approval.ApprovalNotFoundException;
import com.dealflow.backend.approval.ApprovalOutOfSequenceException;
import com.dealflow.backend.approval.ForbiddenApprovalActionException;
import com.dealflow.backend.exception.DealExceptionNotFoundException;
import com.dealflow.backend.exception.ExceptionApprovalRequiredException;
import com.dealflow.backend.exception.InvalidExceptionRequestException;
import com.dealflow.backend.exception.DuplicateExceptionRequestException;
import com.dealflow.backend.cpq.CpqAuthenticationException;
import com.dealflow.backend.cpq.CpqInvalidResponseException;
import com.dealflow.backend.cpq.CpqQuoteNotFoundException;
import com.dealflow.backend.cpq.CpqServiceUnavailableException;
import com.dealflow.backend.cpq.CpqTimeoutException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CpqQuoteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCpqQuoteNotFound(CpqQuoteNotFoundException exception) {
        return cpqResponse(HttpStatus.NOT_FOUND, "CPQ_QUOTE_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(CpqAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleCpqAuthentication(CpqAuthenticationException exception) {
        return cpqResponse(HttpStatus.BAD_GATEWAY, "CPQ_AUTHENTICATION_FAILED",
                "DealFlow could not authenticate with Oracle CPQ.");
    }

    @ExceptionHandler(CpqTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleCpqTimeout(CpqTimeoutException exception) {
        return cpqResponse(HttpStatus.GATEWAY_TIMEOUT, "CPQ_TIMEOUT", exception.getMessage());
    }

    @ExceptionHandler(CpqServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleCpqUnavailable(CpqServiceUnavailableException exception) {
        return cpqResponse(HttpStatus.SERVICE_UNAVAILABLE, "CPQ_UNAVAILABLE", exception.getMessage());
    }

    @ExceptionHandler(CpqInvalidResponseException.class)
    public ResponseEntity<Map<String, Object>> handleCpqInvalidResponse(CpqInvalidResponseException exception) {
        return cpqResponse(HttpStatus.BAD_GATEWAY, "CPQ_INVALID_RESPONSE", exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> cpqResponse(
            HttpStatus status, String code, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("code", code);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(InvalidExceptionRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidExceptionRequest(
            InvalidExceptionRequestException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DuplicateExceptionRequestException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateExceptionRequest(
            DuplicateExceptionRequestException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ExceptionApprovalRequiredException.class)
    public ResponseEntity<Map<String, Object>> handleExceptionApprovalRequired(
            ExceptionApprovalRequiredException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(DealExceptionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDealExceptionNotFound(
            DealExceptionNotFoundException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ApprovalOutOfSequenceException.class)
    public ResponseEntity<Map<String, Object>> handleApprovalOutOfSequence(
            ApprovalOutOfSequenceException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(ApprovalNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleApprovalNotFound(
            ApprovalNotFoundException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ForbiddenApprovalActionException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenApprovalAction(
            ForbiddenApprovalActionException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(InvalidDealTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDealTransition(
            InvalidDealTransitionException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(DealNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDealNotFound(
            DealNotFoundException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
        MethodArgumentNotValidException exception) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();

    exception.getBindingResult()
        .getFieldErrors()
        .forEach(error -> fieldErrors.put(
            error.getField(),
            error.getDefaultMessage()
        ));

    Map<String, Object> response = new LinkedHashMap<>();

    response.put("timestamp", LocalDateTime.now());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validation Failed");
    response.put("fieldErrors", fieldErrors);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", "Invalid email or password.");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        response.put("message", "You do not have permission to perform this action.");

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(response);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<Map<String, Object>> handleAuthenticationFailure(
            AuthenticationException exception) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", "Authentication is required to access this resource.");

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(response);
        }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleMethodValidationErrors(
            HandlerMethodValidationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getParameterValidationResults().forEach(result ->
            result.getResolvableErrors().forEach(error ->
                fieldErrors.put(
                    result.getMethodParameter().getParameterName(),
                    error.getDefaultMessage()
                )
            )
        );

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("fieldErrors", fieldErrors);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }
}