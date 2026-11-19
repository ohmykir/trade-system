package org.chistyakov.tradesystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "seccode", nullable = false)
    private String seccode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buy_order_id",  nullable = false)
    private Order buyOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sell_order_id",  nullable = false)
    private Order sellOrder;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt;

    @PrePersist
    void onCreate() {
        executedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof Trade) return this.id.equals(((Trade) o).getId());
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}