package org.chistyakov.tradesystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateOrderRequest(
        @NotNull @Positive BigDecimal price,
        @Positive int quantity
) {
}
