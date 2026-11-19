package org.chistyakov.tradesystem.dto;

import org.chistyakov.tradesystem.entity.OrderStatus;
import org.chistyakov.tradesystem.entity.OrderType;

import java.time.Instant;

public record OrderFilter(
        Long accountId,
        String seccode,
        OrderType type,
        OrderStatus status,
        Instant from,
        Instant to
) {
}