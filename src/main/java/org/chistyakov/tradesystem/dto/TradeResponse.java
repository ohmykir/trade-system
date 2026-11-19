package org.chistyakov.tradesystem.dto;

import org.chistyakov.tradesystem.entity.Trade;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(
        Long id,
        String seccode,
        BigDecimal price,
        int quantity,
        Instant executedAt,
        Long buyerId,
        Long sellerId
) {
    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
                trade.getId(),
                trade.getSeccode(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getExecutedAt(),
                trade.getBuyOrder().getAccountId(),
                trade.getSellOrder().getAccountId()
        );
    }
}
