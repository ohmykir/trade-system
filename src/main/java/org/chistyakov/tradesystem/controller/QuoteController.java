package org.chistyakov.tradesystem.controller;

import lombok.RequiredArgsConstructor;
import org.chistyakov.tradesystem.dto.QuoteResponse;
import org.chistyakov.tradesystem.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping("/{seccode}")
    public ResponseEntity<QuoteResponse> getQuote(
            @PathVariable String seccode,
            @RequestParam(defaultValue = "10") int depth) {
        return ResponseEntity.ok(quoteService.getQuote(seccode, depth));
    }
}