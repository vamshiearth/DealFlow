package com.dealflow.backend.exception;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deals/{dealId}/exceptions")
public class DealExceptionController {

    private final DealExceptionService exceptionService;

    public DealExceptionController(DealExceptionService exceptionService) {
        this.exceptionService = exceptionService;
    }

    @GetMapping
    public List<ExceptionResponse> getExceptions(@PathVariable Long dealId) {
        return exceptionService.getExceptionsForDeal(dealId)
                .stream()
                .map(ExceptionMapper::toResponse)
                .toList();
    }

    @GetMapping("/requirements")
    public List<ExceptionRequirementResponse> getExceptionRequirements(
            @PathVariable Long dealId) {
        return exceptionService.getExceptionRequirements(dealId);
    }

    @PreAuthorize("hasAnyRole('SALES_REP', 'CPQ_ADMIN')")
    @PostMapping
    public ResponseEntity<ExceptionResponse> createException(
            @PathVariable Long dealId,
            @Valid @RequestBody CreateExceptionRequest request) {
        DealException exception = exceptionService.createException(
                dealId,
                request.exceptionType(),
                request.requestedValue(),
                request.standardValue(),
                request.justification()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ExceptionMapper.toResponse(exception));
    }
}