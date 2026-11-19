package org.chistyakov.tradesystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "instruments")
@Getter
@Setter
@NoArgsConstructor
public class Instrument {

    @Id
    @Column(name = "seccode", length = 16)
    private String seccode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private InstrumentType type;

    @Column(name = "lot_size", nullable = false)
    private int lotSize;

    @Column(name = "min_step",  nullable = false, precision = 19, scale = 4)
    private BigDecimal minStep;

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof Instrument instrument) return this.seccode.equals(instrument.seccode);
        return false;
    }

    @Override
    public int hashCode() {
        return seccode.hashCode();
    }
}