package org.chistyakov.tradesystem.dto;

import org.chistyakov.tradesystem.entity.Order;
import org.chistyakov.tradesystem.entity.OrderStatus;
import org.chistyakov.tradesystem.entity.OrderType;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long accountId,
        String seccode,
        OrderType type,
        BigDecimal price,
        int quantity,
        int filledQuantity,
        OrderStatus status,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getAccountId(),
                order.getSeccode(),
                order.getType(),
                order.getPrice(),
                order.getQuantity(),
                order.getFilledQuantity(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
