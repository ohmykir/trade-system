package org.chistyakov.tradesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.chistyakov.tradesystem.entity.OrderType;

import java.math.BigDecimal;

public record OrderRequest(
        @NotNull Long accountId,
        @NotBlank String seccode,
        @NotNull OrderType type,
        @NotNull @Positive BigDecimal price,
        @Positive int quantity
) {
}
