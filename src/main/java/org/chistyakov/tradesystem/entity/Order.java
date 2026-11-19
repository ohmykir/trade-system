package org.chistyakov.tradesystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "seccode", nullable = false)
    private String seccode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 4)
    private OrderType type;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "filled_quantity", nullable = false)
    private int filledQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrderStatus status = OrderStatus.NEW;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public int getRemainingQuantity() {
        return quantity - filledQuantity;
    }

    public void fill (int tradeQuantity) {
        if (tradeQuantity <= 0 || tradeQuantity > getRemainingQuantity()) {
            throw new IllegalArgumentException("Invalid fill quantity: " + tradeQuantity);
        }

        filledQuantity += tradeQuantity;
        status = filledQuantity == quantity ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof Order) return this.id.equals(((Order)o).getId());
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}