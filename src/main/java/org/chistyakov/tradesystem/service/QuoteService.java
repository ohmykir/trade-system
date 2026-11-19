package org.chistyakov.tradesystem.service;

import lombok.RequiredArgsConstructor;
import org.chistyakov.tradesystem.dto.QuoteLevel;
import org.chistyakov.tradesystem.dto.QuoteResponse;
import org.chistyakov.tradesystem.entity.OrderStatus;
import org.chistyakov.tradesystem.exception.NotFoundException;
import org.chistyakov.tradesystem.repository.InstrumentRepository;
import org.chistyakov.tradesystem.repository.OrderRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final OrderRepository orderRepository;
    private final InstrumentRepository instrumentRepository;

    private static final List<OrderStatus> ACTIVE = List.of(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED);

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public QuoteResponse getQuote(String seccode, int depth){
        if (!instrumentRepository.existsById(seccode)){
            throw new NotFoundException("Instrument not found: " + seccode);
        }

        Limit limit = Limit.of(Math.max(1, depth));
        List<QuoteLevel> bids = orderRepository.findBidLevels(seccode, ACTIVE, limit);
        List<QuoteLevel> asks = orderRepository.findAskLevels(seccode, ACTIVE, limit);

        BigDecimal bestBid = bids.isEmpty() ? null : bids.getFirst().price();
        BigDecimal bestAsk = asks.isEmpty() ? null : asks.getFirst().price();
        BigDecimal spread = (bestBid != null && bestAsk != null) ? bestAsk.subtract(bestBid) : null;

        return new QuoteResponse(seccode, bestBid, bestAsk, spread, bids, asks);
    }
}
