package org.chistyakov.tradesystem.service;

import lombok.RequiredArgsConstructor;
import org.chistyakov.tradesystem.dto.OrderRequest;
import org.chistyakov.tradesystem.dto.UpdateOrderRequest;
import org.chistyakov.tradesystem.entity.*;
import org.chistyakov.tradesystem.exception.IdempotencyConflictException;
import org.chistyakov.tradesystem.exception.NotFoundException;
import org.chistyakov.tradesystem.exception.OrderConflictException;
import org.chistyakov.tradesystem.exception.OrderValidationException;
import org.chistyakov.tradesystem.repository.AccountRepository;
import org.chistyakov.tradesystem.repository.InstrumentRepository;
import org.chistyakov.tradesystem.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MatchingService matchingService;
    private final OrderRepository orderRepository;
    private final InstrumentRepository instrumentRepository;
    private final AccountRepository accountRepository;

    public record Result(Order order, boolean created) {}

    public record Locked(Instrument instrument, Order order, Account account) {}

    @Transactional
    public Result createOrder(OrderRequest request, String idempotencyKey) {
        checkKey(idempotencyKey);

        Instrument instrument = lockInstrument(request.seccode());

        Account account = lockAccount(request.accountId());

        Optional<Order> orderFromDb = orderRepository.findByAccountIdAndIdempotencyKey(request.accountId(), idempotencyKey);
        if (orderFromDb.isPresent()) {
            Order order = orderFromDb.get();
            if (!matches(order, request.seccode(), request.type(), request.price(), request.quantity())) {
                throw new IdempotencyConflictException("Idempotency-Key was already used with different request");
            }

            return new Result(order, false);
        }

        Order saved = placeOrder(instrument, account, request.seccode(), request.type(), request.price(),
                request.quantity(), idempotencyKey);

        return new Result(saved, true);
    }

    @Transactional
    public Result updateOrder(UpdateOrderRequest request, String idempotencyKey, Long id) {
        checkKey(idempotencyKey);

        Locked locked = lockForChange(id);
        Order old = locked.order();

        Optional<Order> orderFromDb = orderRepository.findByAccountIdAndIdempotencyKey(old.getAccountId(), idempotencyKey);
        if (orderFromDb.isPresent()) {
            Order replacement = orderFromDb.get();
            if (!matches(replacement, old.getSeccode(), old.getType(), request.price(), request.quantity())) {
                throw new IdempotencyConflictException("Idempotency-Key was already used with different request");
            }

            return new Result(replacement, false);
        }

        if (!(old.getStatus() == OrderStatus.NEW || old.getStatus() == OrderStatus.PARTIALLY_FILLED)) {
            throw new OrderConflictException("Order " + id + " is " +  old.getStatus() + " and cannot be changed");
        }

        cancelActive(old, locked.instrument(), locked.account());
        Order saved = placeOrder(locked.instrument(), locked.account(), old.getSeccode(), old.getType(),
                                 request.price(), request.quantity(), idempotencyKey);

        return new Result(saved, true);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Locked locked = lockForChange(id);
        Order order = locked.order();

        switch(order.getStatus()) {
            case CANCELLED -> { }
            case FILLED -> throw new OrderConflictException("Order " + id + " is already cancelled");
            default -> cancelActive(order, locked.instrument(), locked.account());
        }
    }

    private void checkKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new OrderValidationException("Idempotency-Key is null or empty");
        }
        if (idempotencyKey.length() <= 64) {
            throw new OrderValidationException("Idempotency-Key must be no longer than 64 characters");
        }
    }

    private Order placeOrder(Instrument instrument, Account account, String seccode, OrderType type, BigDecimal price,
                             int quantity, String idempotencyKey) {
        if (price.remainder(instrument.getMinStep()).signum() != 0) {
            throw new OrderValidationException("Price must be a multiple of min step " +
                    instrument.getMinStep().stripTrailingZeros().toPlainString());
        }

        if (type == OrderType.BUY) {
            reserveFunds(account, price, quantity, instrument.getLotSize());
        }

        Order order = new Order();
        order.setSeccode(seccode);
        order.setAccountId(account.getId());
        order.setType(type);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setIdempotencyKey(idempotencyKey);

        Order saved  = orderRepository.save(order);
        matchingService.match(saved, instrument);

        return saved;
    }

    private Locked lockForChange(Long id) {
        String seccode = orderRepository.findSeccodeById(id)
                                        .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        Instrument instrument = lockInstrument(seccode);

        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found: " + id));
        Account account = lockAccount(order.getAccountId());

        return new Locked(instrument, order, account);
    }

    private Instrument lockInstrument(String seccode) {
        return instrumentRepository.findByIdForUpdate(seccode)
                .orElseThrow(() -> new NotFoundException("Instrument not found: " + seccode));
    }

    private Account lockAccount(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Account not found: " + id));
    }

    private void cancelActive(Order order, Instrument instrument, Account account) {
        if (order.getType() == OrderType.BUY) {
            BigDecimal units = BigDecimal.valueOf((long) order.getRemainingQuantity() * instrument.getLotSize());
            account.setReserved(account.getReserved().subtract(order.getPrice().multiply(units)));
        }

        order.setStatus(OrderStatus.CANCELLED);
    }

    private boolean matches(Order order, String seccode, OrderType type, BigDecimal price, int quantity) {
        return order.getSeccode().equals(seccode)
                && order.getType() == type
                && order.getPrice().compareTo(price) == 0
                && order.getQuantity() == quantity;
    }

    private void reserveFunds(Account account, BigDecimal price, int quantity, int lotSize) {
        BigDecimal required = price.multiply(BigDecimal.valueOf((long) quantity * lotSize));
        BigDecimal available = account.getBalance().subtract(account.getReserved());

        if (available.compareTo(required) < 0) {
            throw new OrderValidationException("Not enough funds to reserve, required: " + required.toPlainString() +
                                                                        ", available: " + available.toPlainString());
        }

        account.setReserved(account.getReserved().add(required));
    }
}