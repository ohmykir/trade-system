package org.chistyakov.tradesystem.service;

import lombok.RequiredArgsConstructor;
import org.chistyakov.tradesystem.entity.*;
import org.chistyakov.tradesystem.exception.NotFoundException;
import org.chistyakov.tradesystem.repository.AccountRepository;
import org.chistyakov.tradesystem.repository.OrderRepository;
import org.chistyakov.tradesystem.repository.TradeRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final AccountRepository accountRepository;

    private static final List<OrderStatus> ACTIVE = List.of(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED);
    private static final int BATCH_SIZE = 10;

    @Transactional
    public void match(Order order, Instrument instrument) {
        while (order.getRemainingQuantity() > 0) {
            List<Order> batch = findBestCounterOrder(order);
            if (batch.isEmpty()) {
                break;
            }

            for (Order best : batch) {
                if (order.getRemainingQuantity() == 0) {
                    break;
                }
                execute(order, best, instrument);
            }

            if (batch.size() < BATCH_SIZE) {
                break;
            }
        }
    }

    private List<Order> findBestCounterOrder(Order order) {
        return order.getType() == OrderType.BUY
                ? orderRepository.findBestSells(order.getSeccode(), order.getPrice(), ACTIVE, Limit.of(BATCH_SIZE))
                : orderRepository.findBestBuys(order.getSeccode(), order.getPrice(), ACTIVE, Limit.of(BATCH_SIZE));
    }

    private void execute(Order order, Order best, Instrument instrument) {
        int tradeQuantity = Math.min(order.getRemainingQuantity(), best.getRemainingQuantity());
        BigDecimal price = best.getPrice();

        Order buy = order.getType() == OrderType.BUY ? order : best;
        Order sell = order.getType() == OrderType.SELL ? order : best;

        BigDecimal units = BigDecimal.valueOf((long) tradeQuantity * instrument.getLotSize());
        BigDecimal amount = price.multiply(units);
        BigDecimal reserved = buy.getPrice().multiply(units);

        Account buyer = lockAccount(buy.getAccountId());
        Account seller = lockAccount(sell.getAccountId());

        buyer.setBalance(buyer.getBalance().subtract(amount));
        buyer.setReserved(buyer.getReserved().subtract(reserved));
        seller.setBalance(seller.getBalance().add(amount));

        order.fill(tradeQuantity);
        best.fill(tradeQuantity);

        Trade trade =  new Trade();
        trade.setSeccode(order.getSeccode());
        trade.setBuyOrder(buy);
        trade.setSellOrder(sell);
        trade.setPrice(price);
        trade.setQuantity(tradeQuantity);
        tradeRepository.save(trade);
    }

    private Account lockAccount(Long accountId) {
        return accountRepository.findByIdForUpdate(accountId).
                orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
    }
}
