package org.chistyakov.tradesystem.dto;

import java.math.BigDecimal;
import java.util.List;

public record QuoteResponse(
        String seccode,
        BigDecimal bestBid,
        BigDecimal bestAsk,
        BigDecimal spread,
        List<QuoteLevel> bids,
        List<QuoteLevel> asks
) {
}
