package org.chistyakov.tradesystem.controller;

import lombok.RequiredArgsConstructor;
import org.chistyakov.tradesystem.dto.TradeFilter;
import org.chistyakov.tradesystem.dto.TradeResponse;
import org.chistyakov.tradesystem.service.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping
    public ResponseEntity<Page<TradeResponse>> getTrades(
            TradeFilter filter,
            @PageableDefault(size = 20, sort = {"executedAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(tradeService.getTrades(filter, pageable));
    }
}
