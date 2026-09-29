package com.dealflow.backend.cpq;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cpq")
public class CpqController {

    private final CpqService cpqService;

    public CpqController(CpqService cpqService) {
        this.cpqService = cpqService;
    }

    @PreAuthorize("hasAnyRole('SALES_REP', 'SALES_MANAGER', 'CPQ_ADMIN')")
    @GetMapping("/quotes/{quoteNumber}")
    public CpqQuoteDTO getQuote(@PathVariable String quoteNumber) {
        return cpqService.getQuote(quoteNumber);
    }

    @PreAuthorize("hasAnyRole('SALES_REP', 'SALES_MANAGER', 'CPQ_ADMIN')")
    @GetMapping("/quotes/{quoteNumber}/details")
    public CpqQuoteDetailDTO getQuoteDetails(@PathVariable String quoteNumber) {
        return cpqService.getQuoteDetails(quoteNumber);
    }
}
