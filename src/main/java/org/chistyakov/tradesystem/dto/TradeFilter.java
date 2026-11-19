package org.chistyakov.tradesystem.dto;

import java.time.Instant;

public record TradeFilter(
        Long accountId,
        String seccode,
        Instant from,
        Instant to
) {
}
