package com.dealflow.backend.exception;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exceptions")
public class DealExceptionDecisionController {

    private final DealExceptionService exceptionService;

    public DealExceptionDecisionController(DealExceptionService exceptionService) {
        this.exceptionService = exceptionService;
    }

    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE', 'VP_SALES', 'CPQ_ADMIN')")
    @PostMapping("/{id}/approve")
    public ExceptionResponse approve(@PathVariable Long id) {
        return ExceptionMapper.toResponse(exceptionService.approveException(id));
    }

    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE', 'VP_SALES', 'CPQ_ADMIN')")
    @PostMapping("/{id}/reject")
    public ExceptionResponse reject(@PathVariable Long id) {
        return ExceptionMapper.toResponse(exceptionService.rejectException(id));
    }

    @GetMapping("/pending")
    public List<ExceptionQueueResponse> getPendingExceptions(
            @RequestParam(required = false) ExceptionType type) {
        return exceptionService.getActivePendingExceptions(type)
                .stream()
                .map(ExceptionQueueMapper::toResponse)
                .toList();
    }
}