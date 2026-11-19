package org.chistyakov.tradesystem.dto;

import java.math.BigDecimal;

public record QuoteLevel(
        BigDecimal price,
        Long quantity,
        Long orders
) {
}
